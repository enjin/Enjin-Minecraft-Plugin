package com.enjin.emp.bungee;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.net.ssl.SSLHandshakeException;

import com.enjin.officialplugin.packets.PacketUtilities;

import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.config.ListenerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.api.scheduler.ScheduledTask;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;

public class EnjinPlugin extends Plugin {

    public static boolean usingSSL = true;
    /**
     * Key is the config value, value is the type, string, boolean, etc.
     */
    public ConcurrentHashMap<String, ConfigValueTypes> configvalues = new ConcurrentHashMap<String, ConfigValueTypes>();
    static public String apiurl = "://api.enjin.com/api/";
    //static public String apiurl = "://gamers.enjin.ca/api/";
    //static public String apiurl = "://tuxreminder.info/api/";
    //static public String apiurl = "://mxm.enjin.com/api/";
    //static public String apiurl = "://api.enjin.ca/api/";

    public boolean autoupdate = true;
    public final static Logger enjinlogger = Logger.getLogger(EnjinPlugin.class.getName());
    public static boolean debug = false;
    public static String hash = "";

    public static Configuration config;

    public String mcversion = "";
    public boolean unabletocontactenjin = false;
    public boolean authkeyinvalid = false;
    public EnjinErrorReport lasterror = null;
    static int logversion = 1;
    Logger logger;

    public String newversion = "";
    public boolean hasupdate = false;
    public boolean updatefailed = false;

    public String minecraftport = "";

    ScheduledTask checkintask = null;
    NewKeyVerifier verifier = null;

    private static EnjinPlugin instance;

    @Override
    public void onEnable() {
        instance = this;
        logger = getLogger();
        mcversion = BungeeCord.getInstance().getGameVersion();

        ProxyServer.getInstance().getPluginManager().registerCommand(this, new EnjinCommand(this));

        try {
            debug("Initializing internal logger");
            enjinlogger.setLevel(Level.FINEST);
            File logsfolder = new File(getDataFolder().getAbsolutePath() + File.separator + "logs");
            if (!logsfolder.exists()) {
                logsfolder.mkdirs();
            }
            File logsfile = new File(getDataFolder().getAbsolutePath() + File.separator + "logs" + File.separator + "enjin.log");
            if (logsfile.exists()) {
                //Max file size of the enjin log should be less than 5MB.
                if (logsfile.length() > 1024 * 1024 * 5) {
                    logsfile.delete();
                }
            }
            FileHandler fileTxt = new FileHandler(getDataFolder().getAbsolutePath() + File.separator + "logs" + File.separator + "enjin.log", true);
            EnjinLogFormatter formatterTxt = new EnjinLogFormatter();
            fileTxt.setFormatter(formatterTxt);
            enjinlogger.addHandler(fileTxt);
            enjinlogger.setUseParentHandlers(false);
            debug("Logger initialized.");

        } catch (Throwable t) {
            BungeeCord.getInstance().getLogger().warning("[Enjin Bungee Plugin] Couldn't enable EnjinMinecraftPlugin logger! Reason: " + t.getMessage());
            enjinlogger.warning("Couldn't enable EnjinMinecraftPlugin's logger! Reason: " + t.getMessage());
            t.printStackTrace();
        }

        //Let's get the minecraft version.
        String[] versionstring = mcversion.split("\\.");
        try {
            int majorversion = Integer.parseInt(versionstring[0].trim());
            int minorversion = Integer.parseInt(versionstring[1].trim().substring(0, 1));
            int buildnumber = 0;
            if (versionstring.length > 2) {
                try {
                    buildnumber = Integer.parseInt(versionstring[2].substring(0, 1));
                } catch (NumberFormatException e) {

                }
            }
            mcversion = majorversion + "." + minorversion + "." + buildnumber;
            if (majorversion == 1) {
                if (minorversion > 6) {
                    logversion = 2;
                    logger.info("[Enjin Bungee Plugin] MC 1.7.2 or above found, enabling version 2 log handling.");
                } else {
                    logger.info("[Enjin Bungee Plugin] MC 1.6.4 or below found, enabling version 1 log handling.");
                }
            } else if (majorversion > 1) {
                logversion = 2;
                logger.info("[Enjin Bungee Plugin] MC 1.7.2 or above found, enabling version 2 log handling.");
            }
        } catch (Exception e) {
            logger.severe("[Enjin Bungee Plugin] Unable to get server version! Inaccurate XP and log handling may occurr!");
            logger.severe("[Enjin Bungee Plugin] Server Version String: " + mcversion);
        }
        initFiles();
        enableTasks();
    }

    public void enableTasks() {
        checkintask = BungeeCord.getInstance().getScheduler().schedule(this, new PeriodicEnjinTask(this), 1, 1, TimeUnit.MINUTES);
    }

    public void stopTask() {
        if (checkintask != null) {
            checkintask.cancel();
            checkintask = null;
        }
    }

    public void initFiles() {
        Iterator<ListenerInfo> listeners = BungeeCord.getInstance().config.getListeners().iterator();
        if (listeners.hasNext()) {
            ListenerInfo info = listeners.next();
            minecraftport = String.valueOf(info.getHost().getPort());
        }

        File configfile = new File(getDataFolder().toString() + "/config.yml");
        if (!configfile.exists()) {
            config = loadYamlFile(configfile);
            createConfig();
        } else {
            config = loadYamlFile(configfile);
        }
        configvalues.put("debug", ConfigValueTypes.BOOLEAN);
        debug = config.getBoolean("debug", false);
        configvalues.put("authkey", ConfigValueTypes.FORBIDDEN);
        hash = config.getString("authkey", "");
        configvalues.put("https", ConfigValueTypes.BOOLEAN);
        usingSSL = config.getBoolean("https", true);
        configvalues.put("autoupdate", ConfigValueTypes.BOOLEAN);
        autoupdate = config.getBoolean("autoupdate", true);
        apiurl = config.getString("apiurl", apiurl);
    }

    private void createConfig() {
        config.set("debug", debug);
        config.set("authkey", hash);
        config.set("https", usingSSL);
        config.set("autoupdate", autoupdate);
        saveConfig();
    }

    public void saveConfig() {
        try {
            ConfigurationProvider.getProvider(YamlConfiguration.class).save(config, new File(getDataFolder(), "config.yml"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean testHTTPconnection() {
        try {
            URL url = new URL("http://api.enjin.com/ok.html");
            URLConnection con = url.openConnection();
            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
            String inputLine = in.readLine();
            in.close();
            if (inputLine != null && inputLine.startsWith("OK")) {
                return true;
            }
            return false;
        } catch (SocketTimeoutException e) {
            if (debug) {
                e.printStackTrace();
            }
            return false;
        } catch (Throwable t) {
            if (debug) {
                t.printStackTrace();
            }
            return false;
        }
    }

    public boolean testHTTPSconnection() {
        try {
            URL url = new URL("https://api.enjin.com/ok.html");
            URLConnection con = url.openConnection();
            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
            String inputLine = in.readLine();
            in.close();
            if (inputLine != null && inputLine.startsWith("OK")) {
                return true;
            }
            return false;
        } catch (SSLHandshakeException e) {
            if (debug) {
                e.printStackTrace();
            }
            return false;
        } catch (SocketTimeoutException e) {
            if (debug) {
                e.printStackTrace();
            }
            return false;
        } catch (Throwable t) {
            if (debug) {
                t.printStackTrace();
            }
            return false;
        }
    }

    public void noEnjinConnectionEvent() {
        if (!unabletocontactenjin) {
            unabletocontactenjin = true;
            Collection<ProxiedPlayer> players = BungeeCord.getInstance().getPlayers();
            for (ProxiedPlayer player : players) {
                if (player.hasPermission("enjin.notify.connectionstatus")) {
                    TextComponent message = new TextComponent("[Enjin Bungee Plugin] Unable to connect to enjin, please check your settings.");
                    message.setColor(ChatColor.DARK_RED);
                    player.sendMessage(message);
                    message = new TextComponent("If this problem persists please send enjin the results of the /enjin report");
                    message.setColor(ChatColor.DARK_RED);
                    player.sendMessage(message);
                }
            }
        }
    }

    public boolean testWebConnection() {
        try {
            URL url = new URL("http://google.com");
            URLConnection con = url.openConnection();
            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
            String inputLine = in.readLine();
            in.close();
            if (inputLine != null) {
                return true;
            }
            return false;
        } catch (SocketTimeoutException e) {
            if (debug) {
                e.printStackTrace();
            }
            return false;
        } catch (Throwable t) {
            if (debug) {
                t.printStackTrace();
            }
            return false;
        }
    }

    /**
     * @param urls
     * @param queryValues
     * @return 0 = Invalid key, 1 = OK, 2 = Exception encountered.
     * @throws MalformedURLException
     */
    public static int sendAPIQuery(String urls, String... queryValues) throws MalformedURLException {
        URL url = new URL((usingSSL ? "https" : "http") + apiurl + urls);
        StringBuilder query = new StringBuilder();
        try {
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");
            con.setReadTimeout(3000);
            con.setConnectTimeout(3000);
            con.setDoOutput(true);
            con.setDoInput(true);
            for (String val : queryValues) {
                query.append('&');
                query.append(val);
            }
            if (queryValues.length > 0) {
                query.deleteCharAt(0); //remove first &
            }

            con.setRequestProperty("Content-length", String.valueOf(query.length()));
            con.getOutputStream().write(query.toString().getBytes());
            String read = PacketUtilities.readString(new BufferedInputStream(con.getInputStream()));
            debug("Reply from enjin on Enjin Key for url " + url.toString() + " and query " + query.toString() + ": " + read);
            if (read.charAt(0) == '1') {
                return 1;
            }
            return 0;
        } catch (SSLHandshakeException e) {
            enjinlogger.warning("SSLHandshakeException, The plugin will use http without SSL. This may be less secure.");
            BungeeCord.getInstance().getLogger().warning("[Enjin Bungee Plugin] SSLHandshakeException, The plugin will use http without SSL. This may be less secure.");
            usingSSL = false;
            return sendAPIQuery(urls, queryValues);
        } catch (SocketTimeoutException e) {
            enjinlogger.warning("Timeout, the enjin server didn't respond within the required time. Please be patient and report this bug to enjin.");
            BungeeCord.getInstance().getLogger().warning("[Enjin Bungee Plugin] Timeout, the enjin server didn't respond within the required time. Please be patient and report this bug to enjin.");
            return 2;
        } catch (Throwable t) {
            t.printStackTrace();
            enjinlogger.warning("Failed to send query to enjin server! " + t.getClass().getName() + ". Data: " + url + "?" + query.toString());
            BungeeCord.getInstance().getLogger().warning("[Enjin Bungee Plugin] Failed to send query to enjin server! " + t.getClass().getName() + ". Data: " + url + "?" + query.toString());
            return 2;
        }
    }

    public static synchronized void setHash(String hash) {
        EnjinPlugin.hash = hash;
    }

    public static synchronized String getHash() {
        return hash;
    }

    public static boolean supportsUUID() {
        return logversion > 1;
    }

    public static boolean isMineshafterPresent() {
        try {
            Class.forName("mineshafter.MineServer");
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static void debug(String s) {
        if (debug) {
            System.out.println("Enjin Debug: " + s);
        }
        enjinlogger.fine(s);
    }

    public static Configuration loadYamlFile(File file) {

        Configuration config = null;
        if (file.exists()) {
            try {
                config = ConfigurationProvider.getProvider(YamlConfiguration.class).load(file);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            try {
                config = ConfigurationProvider.getProvider(YamlConfiguration.class).load("");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return config;
    }

    public static EnjinPlugin getInstance() {
        return instance;
    }
}
