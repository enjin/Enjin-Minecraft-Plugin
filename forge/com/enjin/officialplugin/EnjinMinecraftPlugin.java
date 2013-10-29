package com.enjin.officialplugin;

import java.io.*;
import java.net.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

import javax.net.ssl.SSLHandshakeException;

import net.minecraft.client.Minecraft;
import net.minecraft.command.CommandHandler;
import net.minecraft.command.ICommand;
import net.minecraft.command.ServerCommandManager;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.common.MinecraftForge;

import com.enjin.officialplugin.heads.CachedHeadData;
import com.enjin.officialplugin.heads.HeadListener;
import com.enjin.officialplugin.heads.HeadLocations;
//import com.enjin.officialplugin.listeners.EnjinStatsListener;
import com.enjin.officialplugin.listeners.CommandListener;
import com.enjin.officialplugin.listeners.NewPlayerChatListener;
import com.enjin.officialplugin.listeners.VotifierListener;
import com.enjin.officialplugin.scheduler.TaskScheduler;
import com.enjin.officialplugin.shop.ShopEnableChat;
import com.enjin.officialplugin.shop.ShopItems;
import com.enjin.officialplugin.shop.ShopListener;
import com.enjin.officialplugin.threaded.AsyncToSyncEventThrower;
import com.enjin.officialplugin.threaded.BanLister;
import com.enjin.officialplugin.threaded.CommandExecuter;
import com.enjin.officialplugin.threaded.ConfigSender;
import com.enjin.officialplugin.threaded.NewKeyVerifier;
import com.enjin.officialplugin.threaded.PeriodicEnjinTask;
import com.enjin.officialplugin.threaded.PeriodicVoteTask;
import com.enjin.officialplugin.threaded.ReportMakerThread;
import com.enjin.officialplugin.threaded.UpdateHeadsThread;
import com.enjin.officialplugin.tpsmeter.MonitorTPS;

import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.Mod.Init;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.Mod.PostInit;
import cpw.mods.fml.common.Mod.PreInit;
import cpw.mods.fml.common.Mod.ServerStarted;
import cpw.mods.fml.common.Mod.ServerStarting;
import cpw.mods.fml.common.Mod.ServerStopping;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStartedEvent;
import cpw.mods.fml.common.event.FMLServerStartingEvent;
import cpw.mods.fml.common.event.FMLServerStoppingEvent;
import cpw.mods.fml.common.registry.TickRegistry;
import cpw.mods.fml.relauncher.Side;

/**
 * 
 * @author OverCaste (Enjin LTE PTD).
 * This software is released under an Open Source license.
 * @copyright Enjin 2012.
 * 
 */

@Mod(modid="EnjinMinecraftPlugin", name="EnjinMinecraftPlugin", version="2.4.9-162")
public class EnjinMinecraftPlugin {

	@Instance("EnjinMinecraftPlugin")
	public static EnjinMinecraftPlugin instance;
	
	//Chat color codes.
	protected static Pattern chatColorPattern = Pattern.compile("(?i)&([0-9A-F])");
	protected static Pattern chatMagicPattern = Pattern.compile("(?i)&([K])");
	protected static Pattern chatBoldPattern = Pattern.compile("(?i)&([L])");
	protected static Pattern chatStrikethroughPattern = Pattern.compile("(?i)&([M])");
	protected static Pattern chatUnderlinePattern = Pattern.compile("(?i)&([N])");
	protected static Pattern chatItalicPattern = Pattern.compile("(?i)&([O])");
	protected static Pattern chatResetPattern = Pattern.compile("(?i)&([R])");

	public static String hash = "";
	MinecraftServer s;
	public static boolean debug = false;
	public boolean collectstats = false;
	public boolean supportsglobalgroups = true;
	public boolean votifierinstalled = false;
	public int xpversion = 0;
	
	public HeadLocations headlocation = new HeadLocations();
	public CachedHeadData headdata = new CachedHeadData(this);
	
	public ShopItems cachedItems = new ShopItems();
	
	//----------------Make sure to change this for every minecraft version!
	public String mcversion = "1.6.2";
	
	int signupdateinterval = 10;
	
	public ShopListener shoplistener = new ShopListener();
	public ShopEnableChat shopEClistener = new ShopEnableChat(shoplistener);
	//Since forge mods can be installed on a client, we want to make sure we only run on a server.
	public boolean enable = true;
	
	/**Key is the config value, value is the type, string, boolean, etc.*/
	public ConcurrentHashMap<String, ConfigValueTypes> configvalues = new ConcurrentHashMap<String, ConfigValueTypes>();
	
	public int statssendinterval = 5;
	
	public final static Logger enjinlogger = Logger.getLogger(EnjinMinecraftPlugin.class .getName());
	
	public CommandExecuter commandqueue = new CommandExecuter(this);
	
	//public StatsServer serverstats = new StatsServer(this);
	//public ConcurrentHashMap<String, StatsPlayer> playerstats = new ConcurrentHashMap<String, StatsPlayer>();
	/**Key is banned player, value is admin that banned the player or blank if the console banned*/
	public ConcurrentHashMap<String, String> bannedplayers = new ConcurrentHashMap<String, String>();
	/**Key is banned player, value is admin that pardoned the player or blank if the console pardoned*/
	public ConcurrentHashMap<String, String> pardonedplayers = new ConcurrentHashMap<String, String>();
	
	
	static public String apiurl = "://api.enjin.com/api/";
	//static public String apiurl = "://gamers.enjin.ca/api/";
	//static public String apiurl = "://tuxreminder.info/api/";
	
	public boolean autoupdate = true;
	public String newversion = "";
	
	public static String BUY_COMMAND = "buy";
	
	public boolean hasupdate = false;
	public boolean updatefailed = false;
	public boolean authkeyinvalid = false;
	public boolean unabletocontactenjin = false;
	static public final String updatejar = "http://resources.guild-hosting.net/1/downloads/emp/";
	public static File datafolder = new File("config" + File.separator + "EnjinMinecraftPlugin");
	
	public AsyncToSyncEventThrower eventthrower = new AsyncToSyncEventThrower(this);
	
	public final EMPListener listener = new EMPListener(this);
	public final HeadListener headListener = new HeadListener(this);
	public VotifierListener votelistener;
	
	//------------Threaded tasks---------------
	final PeriodicEnjinTask task = new PeriodicEnjinTask(this);
	final PeriodicVoteTask votetask = new PeriodicVoteTask(this);
	public BanLister banlistertask;
	//Initialize in the onEnable
	public MonitorTPS tpstask;
	//-------------Thread IDS-------------------
	int synctaskid = -1;
	int commandexectuerthread = -1;
	int votetaskid = -1;
	int banlisttask = -1;
	int tpstaskid = -1;
	int headsupdateid = -1;
	
	public TaskScheduler scheduler = new TaskScheduler();
	
	public static final ExecutorService exec = Executors.newCachedThreadPool();
	public static String minecraftport;
	public static boolean usingSSL = true;
	public NewKeyVerifier verifier = null;
	public ConcurrentHashMap<String, String> playerperms = new ConcurrentHashMap<String, String>();
	//Player, lists voted on.
	public ConcurrentHashMap<String, String> playervotes = new ConcurrentHashMap<String, String>();
	
	public EnjinErrorReport lasterror = null;
	
	//public EnjinStatsListener esl = null;
	
	DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss z");

	public EnjinConfig config;
	
	public static void debug(String s) {
		if(debug) {
			System.out.println("Enjin Debug: " + s);
		}
		enjinlogger.fine(s);
	}
	
	@EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		if(event.getSide() == Side.CLIENT) {
			enable = false;
		}else {
			debug("Begin init");
			initFiles();
			headlocation.loadHeads();
			debug("Init files done.");
			try {
				initVariables();
			} catch (Throwable e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			debug("Initializing internal logger");
			enjinlogger.setLevel(Level.FINEST);
			File logsfolder = new File(getDataFolder().getAbsolutePath() + File.separator + "logs");
			if(!logsfolder.exists()) {
				logsfolder.mkdirs();
			}
			FileHandler fileTxt;
			try {
				fileTxt = new FileHandler(getDataFolder().getAbsolutePath() + File.separator + "logs" + File.separator + "enjin.log", true);
				EnjinLogFormatter formatterTxt = new EnjinLogFormatter();
			    fileTxt.setFormatter(formatterTxt);
			    enjinlogger.addHandler(fileTxt);
			} catch (SecurityException e) {
				s.logWarning("[EnjinMinecraftPlugin] Unable to enable debug logging!");
			} catch (IOException e) {
				s.logWarning("[EnjinMinecraftPlugin] Unable to enable debug logging!");
			}
		    enjinlogger.setUseParentHandlers(false);
			debug("Init vars done.");
		}
	}
	
	@EventHandler
	public void onEnable(FMLInitializationEvent event) {
		if(!enable) {
			return;
		}
		try {
			//initPlugins();
			//debug("Init plugins done.");
			//setupPermissions();
			//debug("Setup permissions integration");
			//setupVotifierListener();
			//debug("Setup Votifier integration");
			
			//Register the tick listener
			
			//Let's send the config file in the background
			Thread configthread = new Thread(new ConfigSender(this));
			configthread.start();
			
			
		}
		catch(Throwable t) {
			MinecraftServer.getServer().logWarning("[Enjin Minecraft Plugin] Couldn't enable EnjinMinecraftPlugin! Reason: " + t.getMessage());
			enjinlogger.warning("Couldn't enable EnjinMinecraftPlugin! Reason: " + t.getMessage());
			t.printStackTrace();
			enable = false;
		}
	}



	@EventHandler
	public void serverStarting(FMLServerStartingEvent ev) {
		s = ev.getServer();
		if(s.getCommandManager() instanceof ServerCommandManager) {
			ServerCommandManager scm = (ServerCommandManager)s.getCommandManager();
			scm.registerCommand(new CommandListener(this));
			scm.registerCommand(shoplistener);
			scm.registerCommand(shopEClistener);
		}
	}
	
	@EventHandler
	public void serverStarted(FMLServerStartedEvent event) {
		debug("Enabling Ban lister.");
		banlistertask = new BanLister(this);
		TickRegistry.registerTickHandler(scheduler, Side.SERVER);
		scheduler.runTaskTimerAsynchronously(tpstask = new MonitorTPS(this), 40, 40);
		/*
		if(collectstats) {
			//startStatsCollecting();
			File stats = new File("stats.stats");
			if(stats.exists()) {
				try {
					FileInputStream input = new FileInputStream(stats);
					EnjinStats.Server serverstats = EnjinStats.Server.parseFrom(input);
					debug("Parsing stats input.");
					this.serverstats = new StatsServer(this, serverstats);
					for(EnjinStats.Server.Player player : serverstats.getPlayersList()) {
						debug("Adding player " + player.getName() + ".");
						playerstats.put(player.getName().toLowerCase(), new StatsPlayer(player));
					}
				} catch (FileNotFoundException e) {
				} catch (IOException e) {
				}
			}
			
			MinecraftForge.EVENT_BUS.register(new NewPlayerChatListener(this));
		}*/
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
	
	@EventHandler
	public void serverStopped(FMLServerStoppingEvent event) {
		stopTask();
		scheduler.cancelAllTasks();
		unregisterEvents();
		/*
		if(collectstats) {
			//stopStatsCollecting();
			new WriteStats(this).write("stats.stats");
			debug("Stats saved to stats.stats.");
		}*/
	}

	public File getDataFolder() {
		return datafolder;
	}

	@EventHandler
	public void postInit(FMLPostInitializationEvent event) {
		
	}
	/*
	public void stopStatsCollecting() {
		MinecraftForge.EVENT_BUS.unregister(esl);
	}
	
	public void startStatsCollecting() {
		if(esl == null) {
			esl = new EnjinStatsListener(this);
		}
		MinecraftForge.EVENT_BUS.register(esl);
	}*/
	
	private void initVariables() throws Throwable {
		s = MinecraftServer.getServer();
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
	
	public void initFiles() {
		config = getConfig();
		File configfile = new File(getDataFolder().toString() + File.separator + "config.properties");
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
    	String buy = config.getString("buycommand");
    	if(buy == null) {
    		createConfig();
    	}
    	BUY_COMMAND = config.getString("buycommand", null);
    	
	}
	
	/**
	 * This class simulates the bukkit config file, although in this case it's just a .properties file.
	 * @return
	 */
	private EnjinConfig getConfig() {
		return new EnjinConfig(datafolder);
	}

	private void createConfig() {
		config.set("debug", debug);
		config.set("authkey", hash);
		config.set("https", usingSSL);
		config.set("autoupdate", autoupdate);
		config.set("collectstats", collectstats);
		config.set("sendstatsinterval", statssendinterval);
		config.set("buycommand", BUY_COMMAND);
		config.save();
	}

	public void startTask() {
		debug("Starting tasks.");
		synctaskid = scheduler.runTaskTimerAsynchronously(task, 1200, 1200);
		//Start the command executer a little after the task timer.
		commandexectuerthread = scheduler.runTaskTimerAsynchronously(commandqueue, 1300, 1200);
		banlisttask = scheduler.runTaskTimerAsynchronously(banlistertask, 40, 1800);
		//We want to wait an entire minute before running this to make sure all the worlds have had the time
		//to load before we go and start updating heads.
		headsupdateid = scheduler.runTaskTimerAsynchronously(new UpdateHeadsThread(this), 120, 20*60*signupdateinterval);
		//Only start the vote task if votifier is installed.
		if(votifierinstalled) {
			debug("Starting votifier task.");
			votetaskid = scheduler.runTaskTimerAsynchronously(votetask, 80, 80);
		}
	}
	
	public void registerEvents() {
		debug("Registering events.");
		MinecraftForge.EVENT_BUS.register(listener);
		MinecraftForge.EVENT_BUS.register(headListener);
		if(Loader.instance().isModLoaded("Votifier")) {
			if(votelistener == null) {
				votelistener = new VotifierListener(this);
			}
			MinecraftForge.EVENT_BUS.register(votelistener);
			votifierinstalled = true;
		}
		if (BUY_COMMAND != null) {
			MinecraftForge.EVENT_BUS.register(shoplistener);
		}
	}
	
	public void stopTask() {
		debug("Stopping tasks.");
		if(synctaskid != -1) {
			scheduler.cancelTask(synctaskid);
		}
		if(commandexectuerthread != -1) {
			scheduler.cancelTask(commandexectuerthread);
		}
		if(votetaskid != -1) {
			scheduler.cancelTask(votetaskid);
		}
		if(banlisttask != -1) {
			scheduler.cancelTask(banlisttask);
		}
		if(headsupdateid != -1) {
			scheduler.cancelTask(headsupdateid);
		}
		//Bukkit.getScheduler().cancelTasks(this);
	}
	
	public void unregisterEvents() {
		debug("Unregistering events.");
		try {
			MinecraftForge.EVENT_BUS.unregister(listener);
			MinecraftForge.EVENT_BUS.unregister(headListener);
			if(votelistener != null) {
				MinecraftForge.EVENT_BUS.unregister(votelistener);
			}
		}catch (Exception ex) {
			//Catch NPEs here.
		}
	}
	
	//TODO: Re-add permissions support
	/*
	private void initPlugins() throws Throwable {
		if(!Bukkit.getPluginManager().isPluginEnabled("Vault")) {
			enjinlogger.warning("Couldn't find the vault plugin! Please get it from dev.bukkit.org/server-mods/vault/!");
			getLogger().warning("[Enjin Minecraft Plugin] Couldn't find the vault plugin! Please get it from dev.bukkit.org/server-mods/vault/!");
			return;
		}
		debug("Initializing permissions.");
		initPermissions();
	}
	
	private void initPermissions() throws Throwable {
		RegisteredServiceProvider<Permission> provider = Bukkit.getServicesManager().getRegistration(Permission.class);
		if(provider == null) {
			enjinlogger.warning("Couldn't find a vault compatible permission plugin! Please install one before using the Enjin Minecraft Plugin.");
			Bukkit.getLogger().warning("[Enjin Minecraft Plugin] Couldn't find a vault compatible permission plugin! Please install one before using the Enjin Minecraft Plugin.");
			return;
		}
		permission = provider.getProvider();
		if(permission == null) {
			enjinlogger.warning("Couldn't find a vault compatible permission plugin! Please install one before using the Enjin Minecraft Plugin.");
			Bukkit.getLogger().warning("[Enjin Minecraft Plugin] Couldn't find a vault compatible permission plugin! Please install one before using the Enjin Minecraft Plugin.");
			return;
		}
	}*/
	
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
			MinecraftServer.getServer().logWarning("[Enjin Minecraft Plugin] SSLHandshakeException, The plugin will use http without SSL. This may be less secure.");
			usingSSL = false;
			return sendAPIQuery(urls, queryValues);
		} catch (SocketTimeoutException e) {
			enjinlogger.warning("Timeout, the enjin server didn't respond within the required time. Please be patient and report this bug to enjin.");
			MinecraftServer.getServer().logWarning("[Enjin Minecraft Plugin] Timeout, the enjin server didn't respond within the required time. Please be patient and report this bug to enjin.");
			return 2;
		} catch (Throwable t) {
			t.printStackTrace();
			enjinlogger.warning("Failed to send query to enjin server! " + t.getClass().getName() + ". Data: " + url + "?" + query.toString());
			MinecraftServer.getServer().logWarning("[Enjin Minecraft Plugin] Failed to send query to enjin server! " + t.getClass().getName() + ". Data: " + url + "?" + query.toString());
			return 2;
		}
	}
	
	public static synchronized void setHash(String hash) {
		EnjinMinecraftPlugin.hash = hash;
	}
	
	public static synchronized String getHash() {
		return EnjinMinecraftPlugin.hash;
	}
	
	//TODO: Re-add permissions integration
	/*
	private void setupPermissions() {
    	Plugin pex = this.getServer().getPluginManager().getPlugin("PermissionsEx");
        if (pex != null) {
            permissionsex = (PermissionsEx)pex;
            debug("PermissionsEx found, hooking custom events.");
            Bukkit.getPluginManager().registerEvents(new PexChangeListener(this), this);
            return;
        }
        Plugin bperm = this.getServer().getPluginManager().getPlugin("bPermissions");
        if(bperm != null) {
        	bpermissions = (Permissions)bperm;
            debug("bPermissions found, hooking custom events.");
            supportsglobalgroups = false;
        	Bukkit.getPluginManager().registerEvents(new bPermsChangeListener(this), this);
        	return;
        }
        Plugin groupmanager = this.getServer().getPluginManager().getPlugin("GroupManager");
        if(groupmanager != null) {
        	this.groupmanager = (GroupManager)groupmanager;
            debug("GroupManager found, hooking custom events.");
            supportsglobalgroups = false;
        	Bukkit.getPluginManager().registerEvents(new GroupManagerListener(this), this);
        	return;
        }
        Plugin bukkitperms = this.getServer().getPluginManager().getPlugin("PermissionsBukkit");
        if(bukkitperms != null) {
        	this.permissionsbukkit = (PermissionsPlugin)bukkitperms;
            debug("PermissionsBukkit found, hooking custom events.");
        	Bukkit.getPluginManager().registerEvents(new PermissionsBukkitChangeListener(this), this);
        	return;
        }
        debug("No suitable permissions plugin found, falling back to synching on player disconnect.");
        debug("You might want to switch to PermissionsEx, bPermissions, or Essentials GroupManager.");
        
	}*/

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
	
	/*
	public StatsPlayer GetPlayerStats(String name) {
		StatsPlayer stats = playerstats.get(name.toLowerCase());
		if(stats == null) {
			stats = new StatsPlayer(name);
			playerstats.put(name.toLowerCase(), stats);
		}
		return stats;
	}*/

	public void noEnjinConnectionEvent() {
		if(!unabletocontactenjin) {
			unabletocontactenjin = true;
			Set<String> ops = MinecraftServer.getServer().getConfigurationManager().getOps();
			List<EntityPlayerMP> players = MinecraftServer.getServer().getConfigurationManager().playerEntityList;
			for(EntityPlayerMP player : players) {
				if(ops.contains(player.getEntityName().toLowerCase())) {
					player.addChatMessage(ChatColor.DARK_RED + "[Enjin Minecraft Plugin] Unable to connect to enjin, please check your settings.");
					player.addChatMessage(ChatColor.DARK_RED + "If this problem persists please send enjin the results of the /enjin log");
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

	public String getVersion() {
		return "2.4.9-forge";
	}

	public void forceHeadUpdate() {
		exec.execute(new UpdateHeadsThread(this));
	}
}
