package com.enjin.emp.bungee;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.Proxy;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Plugin;

import com.enjin.emp.bungee.ServerListPing17.StatusResponse;
import com.enjin.officialplugin.packets.Packet14NewerVersion;
import com.enjin.officialplugin.packets.Packet15RemoteConfigUpdate;
import com.enjin.officialplugin.packets.Packet16MultiUserNotice;
import com.enjin.officialplugin.packets.PacketUtilities;

/**
 * @author OverCaste (Enjin LTE PTD).
 *         This software is released under an Open Source license.
 * @copyright Enjin 2012.
 */

public class PeriodicEnjinTask implements Runnable {

    EnjinPlugin plugin;
    ConcurrentHashMap<String, String> removedplayerperms = new ConcurrentHashMap<String, String>();
    ConcurrentHashMap<String, String> removedplayervotes = new ConcurrentHashMap<String, String>();
    HashMap<String, String> removedbans = new HashMap<String, String>();
    HashMap<String, String> removedpardons = new HashMap<String, String>();
    int numoffailedtries = 0;
    int plugindelay = 60;
    boolean firstrun = true;

    public PeriodicEnjinTask(EnjinPlugin plugin) {
        this.plugin = plugin;
    }

    private URL getUrl() throws Throwable {
        return new URL((EnjinPlugin.usingSSL ? "https" : "http") + EnjinPlugin.apiurl + "minecraft-bungeecord");
    }

    @Override
    public void run() {
        //Only run the ssl test on first run.
        if (firstrun && EnjinPlugin.usingSSL) {
            if (!plugin.testHTTPSconnection()) {
                EnjinPlugin.usingSSL = false;
                plugin.getLogger().warning("SSL test connection failed, The plugin will use http without SSL. This may be less secure.");
                EnjinPlugin.enjinlogger.warning("SSL test connection failed, The plugin will use http without SSL. This may be less secure.");
            }
        }
        boolean successful = false;
        StringBuilder builder = new StringBuilder();
        try {
            EnjinPlugin.debug("Connecting to Enjin...");
            URL enjinurl = getUrl();
            HttpURLConnection con;
            // Mineshafter creates a socks proxy, so we can safely bypass it
            // It does not reroute POST requests so we need to go around it
            if (EnjinPlugin.isMineshafterPresent()) {
                con = (HttpURLConnection) enjinurl.openConnection(Proxy.NO_PROXY);
            } else {
                con = (HttpURLConnection) enjinurl.openConnection();
            }
            con.setRequestMethod("POST");
            con.setReadTimeout(15000);
            con.setConnectTimeout(15000);
            con.setDoInput(true);
            con.setDoOutput(true);
            con.setRequestProperty("User-Agent", "Mozilla/4.0");
            con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            //StringBuilder builder = new StringBuilder();
            builder.append("authkey=" + encode(EnjinPlugin.hash));
            if (firstrun) {
                builder.append("&maxplayers=" + encode(String.valueOf(plugin.getProxy().getConfig().getPlayerLimit()))); //max players
                builder.append("&mc_version=" + encode(plugin.mcversion));
            }
            builder.append("&players=" + encode(String.valueOf(ProxyServer.getInstance().getOnlineCount()))); //current players
            builder.append("&pluginversion=" + encode(plugin.getDescription().getVersion()));
            //We only want to send the list of plugins every hour
            if (plugindelay++ >= 59) {
                builder.append("&plugins=" + encode(getPlugins()));
            }
            builder.append("&servers=" + encode(getServers()));
            con.setRequestProperty("Content-Length", String.valueOf(builder.length()));
            EnjinPlugin.debug("Sending content: \n" + builder.toString());
            con.getOutputStream().write(builder.toString().getBytes());
            //System.out.println("Getting input stream...");
            InputStream in = con.getInputStream();
            //System.out.println("Handling input stream...");
            String success = handleInput(in);
            if (success.equalsIgnoreCase("ok")) {
                successful = true;
                if (plugin.unabletocontactenjin) {
                    plugin.unabletocontactenjin = false;
                    Collection<ProxiedPlayer> players = ProxyServer.getInstance().getPlayers();
                    for (ProxiedPlayer player : players) {
                        if (player.hasPermission("enjin.notify.connectionstatus")) {
                            TextComponent message = new TextComponent("[Enjin Bungee Plugin] Connection to Enjin re-established!");
                            message.setColor(ChatColor.DARK_GREEN);
                            player.sendMessage(message);
                            plugin.getLogger().info("Connection to Enjin re-established!");
                        }
                    }
                }
            } else if (success.equalsIgnoreCase("auth_error")) {
                plugin.authkeyinvalid = true;
                EnjinPlugin.enjinlogger.warning("[Enjin Minecraft Plugin] Auth key invalid. Please regenerate on the enjin control panel.");
                plugin.getLogger().warning("Auth key invalid. Please regenerate on the enjin control panel.");
                plugin.stopTask();
                Collection<ProxiedPlayer> players = ProxyServer.getInstance().getPlayers();
                for (ProxiedPlayer player : players) {
                    if (player.hasPermission("enjin.notify.invalidauthkey")) {
                        TextComponent message = new TextComponent("[Enjin Minecraft Plugin] Auth key is invalid. Please generate a new one.");
                        message.setColor(ChatColor.DARK_RED);
                        player.sendMessage(message);
                    }
                }
                successful = false;
            } else if (success.equalsIgnoreCase("bad_data")) {
                EnjinPlugin.enjinlogger.warning("[Enjin Minecraft Plugin] Oops, we sent bad data, please send the enjin.log file to enjin to debug.");
                plugin.lasterror = new EnjinErrorReport("Enjin reported bad data", "Regular synch. Information sent:\n" + builder.toString());
                //plugin.getLogger().warning("Oops, we sent bad data, please send the enjin.log file to enjin to debug.");
                successful = false;
            } else if (success.equalsIgnoreCase("retry_later")) {
                EnjinPlugin.enjinlogger.info("[Enjin Minecraft Plugin] Enjin said to wait, saving data for next sync.");
                //plugin.getLogger().info("Enjin said to wait, saving data for next sync.");
                successful = false;
            } else if (success.equalsIgnoreCase("connect_error")) {
                EnjinPlugin.enjinlogger.info("[Enjin Minecraft Plugin] Enjin is having something going on, if you continue to see this error please report it to enjin.");
                plugin.lasterror = new EnjinErrorReport("Enjin said there's a connection error somewhere.", "Regular synch. Information sent:\n" + builder.toString());
                //plugin.getLogger().info("Enjin is having something going on, if you continue to see this error please report it to enjin.");
                successful = false;
            } else if (success.startsWith("invalid_op")) {
                plugin.lasterror = new EnjinErrorReport(success, "Regular synch. Information sent:\n" + builder.toString());
                successful = false;
            } else {
                EnjinPlugin.enjinlogger.info("[Enjin Minecraft Plugin] Something happened on sync, if you continue to see this error please report it to enjin.");
                EnjinPlugin.enjinlogger.info("Response code: " + success);
                plugin.getLogger().info("Something happened on sync, if you continue to see this error please report it to enjin.");
                plugin.getLogger().info("Response code: " + success);
                successful = false;
            }
            if (!successful) {
                if (numoffailedtries++ > 5 && !plugin.unabletocontactenjin) {
                    numoffailedtries = 0;
                    plugin.noEnjinConnectionEvent();
                }
            } else {
                //If the sync is successful let's reset the number of failed tries
                numoffailedtries = 0;
            }
        } catch (SocketTimeoutException e) {
            //We don't need to spam the console every minute if the synch didn't complete correctly.
            if (numoffailedtries++ > 5) {
                EnjinPlugin.enjinlogger.warning("[Enjin Minecraft Plugin] Timeout, the enjin server didn't respond within the required time. Please be patient and report this bug to enjin.");
                plugin.getLogger().warning("Timeout, the enjin server didn't respond within the required time. Please be patient and report this bug to enjin.");
                numoffailedtries = 0;
                plugin.noEnjinConnectionEvent();
            }
            plugin.lasterror = new EnjinErrorReport(e, "Regular synch. Information sent:\n" + builder.toString());
        } catch (Throwable t) {
            //We don't need to spam the console every minute if the synch didn't complete correctly.
            if (numoffailedtries++ > 5) {
                EnjinPlugin.enjinlogger.warning("[Enjin Minecraft Plugin] Oops, we didn't get a proper response, we may be doing some maintenance. Please be patient and report this bug to enjin if it persists.");
                plugin.getLogger().warning("Oops, we didn't get a proper response, we may be doing some maintenance. Please be patient and report this bug to enjin if it persists.");
                numoffailedtries = 0;
                plugin.noEnjinConnectionEvent();
            }
            if (EnjinPlugin.debug) {
                t.printStackTrace();
            }
            plugin.lasterror = new EnjinErrorReport(t, "Regular synch. Information sent:\n" + builder.toString());
            EnjinPlugin.enjinlogger.warning(plugin.lasterror.toString());
        }
        if (!successful) {
            EnjinPlugin.debug("Synch unsuccessful.");
        } else {
            firstrun = false;
            removedbans.clear();
            removedpardons.clear();
            EnjinPlugin.debug("Synch successful.");
            if (plugindelay >= 59) {
                plugindelay = 0;
            }
        }
    }

    private String encode(String in) throws UnsupportedEncodingException {
        return URLEncoder.encode(in, "UTF-8");
        //return in;
    }

    public String handleInput(InputStream in) throws IOException {
        String tresult = "Unknown Error";
        BufferedInputStream bin = new BufferedInputStream(in);
        bin.mark(Integer.MAX_VALUE);
        for (; ; ) {
            int code = bin.read();
            switch (code) {
                case -1:
                    EnjinPlugin.debug("No more packets. End of stream. Update ended.");
                    bin.reset();
                    StringBuilder input = new StringBuilder();
                    while ((code = bin.read()) != -1) {
                        input.append((char) code);
                    }
                    EnjinPlugin.debug("Raw data received:\n" + input.toString());
                    return tresult; //end of stream reached
                case 0x0A:
                    EnjinPlugin.debug("Packet [0x0A](New Line) received, ignoring...");
                    break;
                case 0x0D:
                    EnjinPlugin.debug("Packet [0x0D](Carriage Return) received, ignoring...");
                    break;
                case 0x14:
                    EnjinPlugin.debug("Packet [0x14](Newer Version) received.");
                    Packet14NewerVersion.handle(bin, plugin);
                    break;
                case 0x15:
                    EnjinPlugin.debug("Packet [0x15](Remote Config Update) received.");
                    Packet15RemoteConfigUpdate.handle(bin, plugin);
                    break;
                case 0x16:
                    EnjinPlugin.debug("Packet [0x16](Multi-user Notice) received.");
                    Packet16MultiUserNotice.handle(bin, plugin);
                    break;
                case 0x19:
                    EnjinPlugin.debug("Packet [0x19](Enjin Status) received.");
                    tresult = PacketUtilities.readString(bin);
                    break;
                case 0x3C:
                    EnjinPlugin.debug("Packet [0x3C](Enjin Maintenance Page) received. Aborting sync.");
                    bin.reset();
                    StringBuilder input1 = new StringBuilder();
                    while ((code = bin.read()) != -1) {
                        input1.append((char) code);
                    }
                    EnjinPlugin.debug("Raw data received:\n" + input1.toString());
                    return "retry_later";
                default:
                    EnjinPlugin.debug("[Enjin] Received an invalid opcode: " + code);
                    bin.reset();
                    StringBuilder input2 = new StringBuilder();
                    while ((code = bin.read()) != -1) {
                        input2.append((char) code);
                    }
                    EnjinPlugin.debug("Raw data received:\n" + input2.toString());
                    return "invalid_op\nRaw data received:\n" + input2.toString();
            }
        }
    }

    private String getPlugins() {
        StringBuilder builder = new StringBuilder();
        Collection<Plugin> plugins = ProxyServer.getInstance().getPluginManager().getPlugins();
        for (Plugin p : plugins) {
            builder.append(',');
            builder.append(p.getDescription().getName());
        }
        if (builder.length() > 2) {
            builder.deleteCharAt(0);
        }
        return builder.toString();
    }

    private String getServers() {
        StringBuilder builder = new StringBuilder();
        Iterator<Entry<String, ServerInfo>> servers = ProxyServer.getInstance().getServers().entrySet().iterator();
        while (servers.hasNext()) {
            Entry<String, ServerInfo> server = servers.next();
            ServerInfo info = server.getValue();
            String name = info.getName();
            int maxplayers = -1;
            try {
                StatusResponse serverdata = ServerListPing17.getServerDetails(server.getValue().getAddress(), 500);

                if (serverdata == null) {
                    continue;
                }

                maxplayers = serverdata.getPlayers().getMax();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            Collection<ProxiedPlayer> players = info.getPlayers();
            if (builder.length() > 0) {
                builder.append(",");
            }
            builder.append("{" + name + "|");
            boolean first = true;
            for (ProxiedPlayer player : players) {
                if (first) {
                    if (EnjinPlugin.supportsUUID()) {
                        builder.append(player.getUniqueId().toString() + ":" + player.getName());
                    } else {
                        builder.append(player.getName());
                    }
                    first = false;
                } else {
                    if (EnjinPlugin.supportsUUID()) {
                        builder.append("," + player.getUniqueId().toString() + ":" + player.getName());
                    } else {
                        builder.append("," + player.getName());
                    }
                }
            }
            builder.append("|" + maxplayers);
            builder.append("}");
        }
        return builder.toString();
    }

    private String base64Encode(String encode) {
        if (encode == null) {
            return "";
        }
        return javax.xml.bind.DatatypeConverter.printBase64Binary(encode.getBytes());
    }
}
