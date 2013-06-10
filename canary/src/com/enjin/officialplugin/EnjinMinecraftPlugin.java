package com.enjin.officialplugin;

import java.io.*;
import java.net.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.net.ssl.SSLHandshakeException;

import net.canarymod.Canary;
import net.canarymod.api.OfflinePlayer;
import net.canarymod.api.Server;
import net.canarymod.api.entity.living.humanoid.Player;
import net.canarymod.chat.Colors;
import net.canarymod.config.Configuration;
import net.canarymod.permissionsystem.PermissionManager;
import net.canarymod.plugin.Plugin;
import net.canarymod.tasks.ServerTaskManager;
import net.canarymod.tasks.TaskOwner;
import net.visualillusionsent.utils.PropertiesFile;
import net.visualillusionsent.utils.TaskManager;

import com.enjin.officialplugin.listeners.EnjinStatsListener;
import com.enjin.officialplugin.listeners.NewPlayerChatListener;
import com.enjin.officialplugin.shop.ShopListener;
import com.enjin.officialplugin.stats.StatsPlayer;
import com.enjin.officialplugin.stats.StatsServer;
import com.enjin.officialplugin.stats.WriteStats;
import com.enjin.officialplugin.threaded.BanLister;
import com.enjin.officialplugin.threaded.CommandExecuter;
import com.enjin.officialplugin.threaded.ConfigSender;
import com.enjin.officialplugin.threaded.NewKeyVerifier;
import com.enjin.officialplugin.threaded.PeriodicEnjinTask;
import com.enjin.officialplugin.threaded.PeriodicVoteTask;
import com.enjin.officialplugin.tpsmeter.MonitorTPS;
import com.enjin.proto.stats.EnjinStats;

/**
 * 
 * @author OverCaste (Enjin LTE PTD).
 * This software is released under an Open Source license.
 * @copyright Enjin 2013.
 * 
 */

public class EnjinMinecraftPlugin extends Plugin implements TaskOwner {

	
	public PropertiesFile config;
	
	public static String hash = "";
	Server s;
	public static Logger logger;
	public static boolean debug = false;
	public boolean collectstats = false;
	public boolean votifierinstalled = false;
	public int xpversion = 0;
	public String mcversion = "";
	
	public static String BUY_COMMAND = "buy";
	
	/**Key is the config value, value is the type, string, boolean, etc.*/
	public ConcurrentHashMap<String, ConfigValueTypes> configvalues = new ConcurrentHashMap<String, ConfigValueTypes>();
	
	public int statssendinterval = 5;
	
	public final static Logger enjinlogger = Logger.getLogger(EnjinMinecraftPlugin.class .getName());
	
	public CommandExecuter commandqueue = new CommandExecuter(this);
	
	public StatsServer serverstats = new StatsServer(this);
	public ConcurrentHashMap<String, StatsPlayer> playerstats = new ConcurrentHashMap<String, StatsPlayer>();
	/**Key is banned player, value is admin that banned the player or blank if the console banned*/
	public ConcurrentHashMap<String, String> bannedplayers = new ConcurrentHashMap<String, String>();
	/**Key is banned player, value is admin that pardoned the player or blank if the console pardoned*/
	public ConcurrentHashMap<String, String> pardonedplayers = new ConcurrentHashMap<String, String>();
	
	
	static public String apiurl = "://api.enjin.com/api/";
	//static public String apiurl = "://gamers.enjin.ca/api/";
	//static public String apiurl = "://tuxreminder.info/api/";
	//static public String apiurl = "://mxm.enjin.com/api/";
	//static public String apiurl = "://api.0x10cbuilder.com/api/";
	
	public boolean autoupdate = true;
	public String newversion = "";
	
	public boolean hasupdate = false;
	public boolean updatefailed = false;
	public boolean authkeyinvalid = false;
	public boolean unabletocontactenjin = false;
	public boolean permissionsnotworking = false;
	static public final String updatejar = "http://resources.guild-hosting.net/1/downloads/emp/";
	static public final String bukkitupdatejar = "http://dev.bukkit.org/media/files/";
	static public boolean bukkitversion = false;
	
	public final EMPListener listener = new EMPListener(this);
	
	//------------Threaded tasks---------------
	final PeriodicEnjinTask task = new PeriodicEnjinTask(this);
	final PeriodicVoteTask votetask = new PeriodicVoteTask(this);
	public BanLister banlistertask;
	//Initialize in the onEnable
	public MonitorTPS tpstask;
	
	static final ExecutorService exec = Executors.newCachedThreadPool();
	public static String minecraftport;
	public static boolean usingSSL = true;
	NewKeyVerifier verifier = null;
	public ConcurrentHashMap<String, String> playerperms = new ConcurrentHashMap<String, String>();
	//Player, lists voted on.
	public ConcurrentHashMap<String, String> playervotes = new ConcurrentHashMap<String, String>();
	
	public EnjinErrorReport lasterror = null;
	
	public EnjinStatsListener esl = null;
	
	DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss z");
	public ShopListener shoplistener;

	public PermissionManager permissions;
	
	public static void debug(String s) {
		if(debug) {
			System.out.println("Enjin Debug: " + s);
		}
		enjinlogger.fine(s);
	}
	
	@Override
	public boolean enable() {
		try {
			debug("Initializing internal logger");
			enjinlogger.setLevel(Level.FINEST);
			File logsfolder = new File("enjinlogs");
			if(!logsfolder.exists()) {
				logsfolder.mkdirs();
			}
			FileHandler fileTxt = new FileHandler("enjinlogs" + File.separator + "enjin.log", true);
			EnjinLogFormatter formatterTxt = new EnjinLogFormatter();
		    fileTxt.setFormatter(formatterTxt);
		    enjinlogger.addHandler(fileTxt);
		    enjinlogger.setUseParentHandlers(false);
		    debug("Logger initialized.");
			debug("Begin init");
			initVariables();
			debug("Init vars done.");
			debug("Get the ban list");
			banlistertask = new BanLister(this);
			debug("Ban list loaded");
			debug("Init Files");
			initFiles();
			debug("Init files done.");
			initPermissions();
			debug("Init plugins done.");
			//setupVotifierListener();
			debug("Setup Votifier integration");
			try {
				Canary.commands().registerCommands(new EnjinCommandListener(this), this, false);
			}catch(Exception e) {
				
				//duplicate commands are such a pain...
			}
			//------We should do TPS even if we have an invalid auth key
			ServerTaskManager.addTask(tpstask = new MonitorTPS(this));
			//TaskManager.scheduleContinuedTask(tpstask = new MonitorTPS(this), 40, 40, TimeUnit.)
			
			Thread configthread = new Thread(new ConfigSender(this));
			configthread.start();
			
			//Let's get the minecraft version.
			//TODO: Wait for canarymod hook.
			//String[] cbversionstring = Canary.getServer().getVersion().split(":");
			
			if(collectstats) {
				startStatsCollecting();
				File stats = new File("stats.stats");
				if(stats.exists()) {
					FileInputStream input = new FileInputStream(stats);
					EnjinStats.Server serverstats = EnjinStats.Server.parseFrom(input);
					debug("Parsing stats input.");
					this.serverstats = new StatsServer(this, serverstats);
					for(EnjinStats.Server.Player player : serverstats.getPlayersList()) {
						debug("Adding player " + player.getName() + ".");
						playerstats.put(player.getName().toLowerCase(), new StatsPlayer(player));
					}
				}
				Canary.hooks().registerListener(new NewPlayerChatListener(this), this);
			}
			//debug("Checking key valid.");
			//Bypass key checking, but only if the key looks valid
			registerEvents();
			if(hash.length() == 50) {
				debug("Starting periodic tasks.");
				startTask();
			}else {
				authkeyinvalid = true;
				debug("Auth key is invalid. Wrong length.");
			}
		}
		catch(Throwable t) {
			logger.warning("[Enjin Minecraft Plugin] Couldn't enable EnjinMinecraftPlugin! Reason: " + t.getMessage());
			enjinlogger.warning("Couldn't enable EnjinMinecraftPlugin! Reason: " + t.getMessage());
			t.printStackTrace();
			return false;
		}
		return true;
	}
	
	/*
	public void stopStatsCollecting() {
		HandlerList.unregisterAll(esl);
	}*/
	
	public void startStatsCollecting() {
		if(esl == null) {
			esl = new EnjinStatsListener(this);
		}
		Canary.hooks().registerListener(esl, this);
	}

	@Override
	public void disable() {
		stopTask();
		//unregisterEvents();
		if(collectstats) {
			new WriteStats(this).write("stats.stats");
			debug("Stats saved to stats.stats.");
		}
	}
	
	private void initVariables() throws Throwable {
		s = Canary.getServer();
		logger = getLogman();
		try {
			Properties serverProperties = new Properties();
			FileInputStream in = new FileInputStream(new File("server.properties"));
			serverProperties.load(in);
			in.close();
			minecraftport = serverProperties.getProperty("server-port");
		} catch (Throwable t) {
			t.printStackTrace();
			enjinlogger.severe("Couldn't find a localhost ip! Please report this problem!");
			throw new Exception("[Enjin Minecraft Plugin] Couldn't find a localhost ip! Please report this problem!");
		}
	}
	
	public Logger getLogger() {
		return logger;
	}
	
	public void initFiles() {
		config = Configuration.getPluginConfig("EnjinMinecraftPlugin");
    	if(config.getString("debug", "").equals("")) {
    		createConfig();
    	}
    	configvalues.put("debug", ConfigValueTypes.BOOLEAN);
    	debug = config.getBoolean("debug", false);
    	configvalues.put("authkey", ConfigValueTypes.FORBIDDEN);
    	hash = config.getString("authkey", "");
    	debug("Key value retrieved: " + hash);
    	configvalues.put("https", ConfigValueTypes.BOOLEAN);
    	usingSSL = config.getBoolean("https", true);
    	configvalues.put("autoupdate", ConfigValueTypes.BOOLEAN);
    	autoupdate = config.getBoolean("autoupdate", true);
    	configvalues.put("collectstats", ConfigValueTypes.BOOLEAN);
    	collectstats = config.getBoolean("collectstats", collectstats);
    	configvalues.put("sendstatsinterval", ConfigValueTypes.INT);
    	statssendinterval = config.getInt("sendstatsinterval", 5);
    	configvalues.put("buycommand", ConfigValueTypes.STRING);
    	BUY_COMMAND = config.getString("buycommand", null);
	}
	
	private void createConfig() {
		config.setBoolean("debug", debug);
		config.setString("authkey", hash);
		config.setBoolean("https", usingSSL);
		config.setBoolean("autoupdate", autoupdate);
		config.setBoolean("collectstats", collectstats);
		config.setInt("sendstatsinterval", statssendinterval);
		config.setString("buycommand", BUY_COMMAND);
    	
		config.save();
	}

	public void startTask() {
		debug("Starting tasks.");
		TaskManager.scheduleContinuedTask(task, 1, 1, TimeUnit.MINUTES);
		TaskManager.scheduleContinuedTask(banlistertask, 2, 90, TimeUnit.SECONDS);
		//Only start the vote task if votifier is installed.
		if(votifierinstalled) {
			debug("Starting votifier task.");
			TaskManager.scheduleContinuedTask(votetask, 4, 4, TimeUnit.SECONDS);
		}
	}
	
	public void registerEvents() {
		debug("Registering events.");
		Canary.hooks().registerListener(listener, this);
		if(BUY_COMMAND != null) {
			shoplistener = new ShopListener();
			Canary.hooks().registerListener(shoplistener, this);
		}
	}
	
	public void stopTask() {
		debug("Stopping tasks.");
		if(task != null) {
			TaskManager.removeTask(task);
		}
		if(votetask != null) {
			TaskManager.removeTask(votetask);
		}
		if(banlistertask != null) {
			TaskManager.removeTask(banlistertask);
		}
	}
	
	public void unregisterEvents() {
		debug("Unregistering events.");
		Canary.hooks().unregisterPluginListeners(this);
	}
	
	//TODO: Port votifier
	/*
	private void setupVotifierListener() {
		if(Bukkit.getPluginManager().isPluginEnabled("Votifier")) {
			System.out.println("[Enjin Minecraft Plugin] Votifier plugin found, enabling Votifier support.");
			enjinlogger.info("Votifier plugin found, enabling Votifier support.");
			Bukkit.getPluginManager().registerEvents(new VotifierListener(this), this);
			votifierinstalled = true;
		}
	}*/
	
	private void initPermissions() {
		permissions = Canary.permissionManager();
	}
	
	/**
	 * 
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
			for(String val : queryValues) {
				query.append('&');
				query.append(val);
			}
			if(queryValues.length > 0) {
				query.deleteCharAt(0); //remove first &
			}
			con.setRequestProperty("Content-length", String.valueOf(query.length()));
			con.getOutputStream().write(query.toString().getBytes());
			if(con.getInputStream().read() == '1') {
				return 1;
			}
			return 0;
		} catch (SSLHandshakeException e) {
			enjinlogger.warning("SSLHandshakeException, The plugin will use http without SSL. This may be less secure.");
			logger.warning("[Enjin Minecraft Plugin] SSLHandshakeException, The plugin will use http without SSL. This may be less secure.");
			usingSSL = false;
			return sendAPIQuery(urls, queryValues);
		} catch (SocketTimeoutException e) {
			enjinlogger.warning("Timeout, the enjin server didn't respond within the required time. Please be patient and report this bug to enjin.");
			logger.warning("[Enjin Minecraft Plugin] Timeout, the enjin server didn't respond within the required time. Please be patient and report this bug to enjin.");
			return 2;
		} catch (Throwable t) {
			t.printStackTrace();
			enjinlogger.warning("Failed to send query to enjin server! " + t.getClass().getName() + ". Data: " + url + "?" + query.toString());
			logger.warning("[Enjin Minecraft Plugin] Failed to send query to enjin server! " + t.getClass().getName() + ". Data: " + url + "?" + query.toString());
			return 2;
		}
	}
	
	public static synchronized void setHash(String hash) {
		EnjinMinecraftPlugin.hash = hash;
	}
	
	public static synchronized String getHash() {
		return EnjinMinecraftPlugin.hash;
	}

	public int getTotalXP(int level, float xp) {
		int atlevel = 0;
		int totalxp = 0;
		int xpneededforlevel = 0;
		if(xpversion == 1) {
			xpneededforlevel = 17;
			while(atlevel < level) {
				atlevel++;
				totalxp += xpneededforlevel;
				if(atlevel >= 16) {
					xpneededforlevel += 3;
				}
			}
			//We only have 2 versions at the moment
		}else {
			xpneededforlevel = 7;
	    	boolean odd = true;
	    	while(atlevel < level) {
	    		atlevel++;
	    		totalxp += xpneededforlevel;
	    		if(odd) {
	    			xpneededforlevel += 3;
	    			odd = false;
	    		}else {
	    			xpneededforlevel += 4;
	    			odd = true;
	    		}
	    	}
		}
		totalxp = (int) (totalxp + (xp*xpneededforlevel));
		return totalxp;
	}
	
	public StatsPlayer GetPlayerStats(String name) {
		StatsPlayer stats = playerstats.get(name.toLowerCase());
		if(stats == null) {
			stats = new StatsPlayer(name);
			playerstats.put(name.toLowerCase(), stats);
		}
		return stats;
	}

	public void noEnjinConnectionEvent() {
		if(!unabletocontactenjin) {
			unabletocontactenjin = true;
			ArrayList<Player> players = Canary.getServer().getPlayerList();
			for(Player player : players) {
				if(player.hasPermission("enjin.notify.connectionstatus")) {
					player.sendMessage(Colors.RED + "[Enjin Minecraft Plugin] Unable to connect to enjin, please check your settings.");
					player.sendMessage(Colors.RED + "If this problem persists please send enjin the results of the /enjin log");
				}
			}
		}
	}

	public boolean testHTTPSconnection() {
		try {
			URL url = new URL("https://api.enjin.com/ok.html");
			URLConnection con = url.openConnection();
			BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
	        String inputLine = in.readLine();
	        in.close();
			if(inputLine != null && inputLine.startsWith("OK")) {
				return true;
			}
			return false;
		} catch (SSLHandshakeException e) {
			if(debug) {
				e.printStackTrace();
			}
			return false;
		} catch (SocketTimeoutException e) {
			if(debug) {
				e.printStackTrace();
			}
			return false;
		} catch (Throwable t) {
			if(debug) {
				t.printStackTrace();
			}
			return false;
		}
	}

	public boolean testWebConnection() {
		try {
			URL url = new URL("http://google.com");
			URLConnection con = url.openConnection();
			BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
	        String inputLine = in.readLine();
	        in.close();
			if(inputLine != null) {
				return true;
			}
			return false;
		} catch (SocketTimeoutException e) {
			if(debug) {
				e.printStackTrace();
			}
			return false;
		} catch (Throwable t) {
			if(debug) {
				t.printStackTrace();
			}
			return false;
		}
	}

	public boolean testHTTPconnection() {
		try {
			URL url = new URL("http://api.enjin.com/ok.html");
			URLConnection con = url.openConnection();
			BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
	        String inputLine = in.readLine();
	        in.close();
			if(inputLine != null && inputLine.startsWith("OK")) {
				return true;
			}
			return false;
		} catch (SocketTimeoutException e) {
			if(debug) {
				e.printStackTrace();
			}
			return false;
		} catch (Throwable t) {
			if(debug) {
				t.printStackTrace();
			}
			return false;
		}
	}

	public static boolean isMineshafterPresent() {
	    try {
	        Class.forName("mineshafter.MineServer");
	        return true;
	    } catch (Exception e) {
	        return false;
	    }
	}
	
	public PeriodicEnjinTask getTask() {
		return task;
	}
	
	public static ArrayList<OfflinePlayer> getAllPlayersData() {
		ArrayList<OfflinePlayer> allplayers = new ArrayList<OfflinePlayer>();
		File playersfolder = new File("worlds" + File.separator + "players");
		if(!playersfolder.exists()) {
			return allplayers;
		}
		File [] files = playersfolder.listFiles();
		for(File file : files) {
			if(!file.isFile()) continue;  //avoids folders
			if(!file.getName().toLowerCase().endsWith(".dat")) continue; //avoids non-player data files.
			String filename = file.getName();
			String playername = filename.substring(0, filename.length() - 4);
			OfflinePlayer offlineplayer = Canary.getServer().getOfflinePlayer(playername);
			allplayers.add(offlineplayer);
		}
		return allplayers;
	}
}
