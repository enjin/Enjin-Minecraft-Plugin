package com.enjin.officialplugin;

import java.io.*;
import java.net.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

import javax.net.ssl.SSLHandshakeException;

import net.milkbowl.vault.permission.Permission;
import net.milkbowl.vault.permission.plugins.Permission_GroupManager;

import org.anjocaido.groupmanager.GroupManager;
import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import de.bananaco.bpermissions.imp.Permissions;

import ru.tehkode.permissions.bukkit.PermissionsEx;

/**
 * 
 * @author OverCaste (Enjin LTE PTD).
 * This software is released under an Open Source license.
 * @copyright Enjin 2012.
 * 
 */

public class EnjinMinecraftPlugin extends JavaPlugin {
	
	public FileConfiguration config;
	public static boolean usingGroupManager = false;
	File hashFile;
	static String hash = "";
	Server s;
	Logger logger;
	static Permission permission = null;
	boolean debug = false;
	PermissionsEx permissionsex;
	GroupManager groupmanager;
	Permissions bpermissions;
	
	boolean autoupdate = true;
	String newversion = "";
	
	boolean hasupdate = false;
	static public final String updatejar = "http://resources.guild-hosting.net/1/downloads/EnjinMinecraftPlugin.jar";
	
	final EMPListener listener = new EMPListener(this);
	final PeriodicEnjinTask task = new PeriodicEnjinTask(this);
	static final ExecutorService exec = Executors.newCachedThreadPool();
	static String minecraftport;
	static boolean usingSSL = true;
	NewKeyVerifier verifier = null;
	ConcurrentHashMap<PlayerPerms, String[]> playerperms = new ConcurrentHashMap<PlayerPerms, String[]>();
	
	void debug(String s) {
		if(debug) {
			System.out.println("Enjin Debug: " + s);
		}
	}
	
	@Override
	public void onEnable() {
		try {
			debug("Begin init");
			initVariables();
			debug("Init vars done.");
			initFiles();
			debug("Init files done.");
			initPlugins();
			debug("Init plugins done.");
			setupPermissions();
			debug("Setup permissions integration");
			usingGroupManager = (permission instanceof Permission_GroupManager);
			debug("Checking key valid.");
			if(verifier == null || verifier.completed) {
				verifier = new NewKeyVerifier(this, hash, null, true);
				Thread verifierthread = new Thread(verifier);
				verifierthread.start();
			}else {
				Bukkit.getLogger().warning("[Enjin Minecraft Plugin] A key verification is already running. Did you /reload?");
			}
		}
		catch(Throwable t) {
			Bukkit.getLogger().warning("[Enjin Minecraft Plugin] Couldn't enable EnjinMinecraftPlugin! Reason: " + t.getMessage());
			t.printStackTrace();
			this.setEnabled(false);
		}
	}
	
	@Override
	public void onDisable() {
		stopTask();
		//unregisterEvents();
	}
	
	private void initVariables() throws Throwable {
		hashFile = new File(this.getDataFolder(), "HASH.txt");
		s = Bukkit.getServer();
		logger = Bukkit.getLogger();
		try {
			Properties serverProperties = new Properties();
			FileInputStream in = new FileInputStream(new File("server.properties"));
			serverProperties.load(in);
			in.close();
			minecraftport = serverProperties.getProperty("server-port");
		} catch (Throwable t) {
			t.printStackTrace();
			throw new Exception("[Enjin Minecraft Plugin] Couldn't find a localhost ip! Please report this problem!");
		}
	}
	
	private void initFiles() {
		//let's read in the old hash file if there is one and convert it to the new format.
		if(hashFile.exists()) {
			try {
				BufferedReader r = new BufferedReader(new FileReader(hashFile));
				hash = r.readLine();
				r.close();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			//Remove it, we won't ever need it again.
			hashFile.delete();
		}
		config = getConfig();
		File configfile = new File(getDataFolder().toString() + "/config.yml");
    	if(!configfile.exists()) {
    		createConfig();
    	}
    	debug = config.getBoolean("debug", false);
    	hash = config.getString("authkey", "");
    	usingSSL = config.getBoolean("https", true);
    	autoupdate = config.getBoolean("autoupdate", true);
	}
	
	private void createConfig() {
		config.set("debug", false);
		config.set("authkey", hash);
		config.set("https", true);
		config.set("autoupdate", true);
		saveConfig();
	}

	void startTask() {
		debug("Starting task.");
		Bukkit.getScheduler().scheduleAsyncRepeatingTask(this, task, 1200L, 1200L);
	}
	
	void registerEvents() {
		debug("Registering events.");
		Bukkit.getPluginManager().registerEvents(listener, this);
	}
	
	void stopTask() {
		debug("Stopping task.");
		Bukkit.getScheduler().cancelTasks(this);
	}
	
	void unregisterEvents() {
		debug("Unregistering events.");
		HandlerList.unregisterAll(listener);
	}
	
	private void initPlugins() throws Throwable {
		if(!Bukkit.getPluginManager().isPluginEnabled("Vault")) {
			throw new Exception("[Enjin Minecraft Plugin] Couldn't find the vault plugin! Please get it from dev.bukkit.org/server-mods/vault/!");
		}
		debug("Initializing permissions.");
		initPermissions();
	}
	
	private void initPermissions() throws Throwable {
		RegisteredServiceProvider<Permission> provider = Bukkit.getServicesManager().getRegistration(Permission.class);
		if(provider == null) {
			Bukkit.getLogger().warning("[Enjin Minecraft Plugin] Couldn't find a vault compatible permission plugin! Please install one before using the Enjin Minecraft Plugin.");
			return;
		}
		permission = provider.getProvider();
		if(permission == null) {
			Bukkit.getLogger().warning("[Enjin Minecraft Plugin] Couldn't find a vault compatible permission plugin! Please install one before using the Enjin Minecraft Plugin.");
			return;
		}
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if(command.getName().equals("enjinkey")) {
			if(!sender.hasPermission("enjin.setkey")) {
				sender.sendMessage(ChatColor.RED + "You need to have the \"enjin.setkey\" permission or OP to run that command!");
				return true;
			}
			if(args.length != 1) {
				return false;
			}
			Bukkit.getLogger().info("Checking if key is valid");
			//Make sure we don't have several verifier threads going at the same time.
			if(verifier == null || verifier.completed) {
				verifier = new NewKeyVerifier(this, args[0], sender, false);
				Thread verifierthread = new Thread(verifier);
				verifierthread.start();
			}else {
				sender.sendMessage(ChatColor.RED + "Please wait until we verify the key before you try again!");
			}
			return true;
		}if (command.getName().equalsIgnoreCase("enjin")) {
			if(args.length > 0) {
				if(args[0].equalsIgnoreCase("report")) {
					if(!sender.hasPermission("enjin.report")) {
						sender.sendMessage(ChatColor.RED + "You need to have the \"enjin.report\" permission or OP to run that command!");
						return true;
					}
					sender.sendMessage(ChatColor.GREEN + "Please wait as we generate the report");
					DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
					Date date = new Date();
					StringBuilder report = new StringBuilder();
					report.append("Enjin Debug Report generated on " + dateFormat.format(date) + "\n");
					report.append("Enjin plugin version: " + getDescription().getVersion() + "\n");
					String permsmanager = "Generic";
					String permsversion = "Unknown";
					if(permissionsex != null) {
						permsmanager = "PermissionsEx";
						permsversion = permissionsex.getDescription().getVersion();
					}else if(bpermissions != null) {
						permsmanager = "bPermissions";
						permsversion = bpermissions.getDescription().getVersion();
					}else if(groupmanager != null) {
						permsmanager = "GroupManager";
						permsversion = groupmanager.getDescription().getVersion();
					}
					report.append("Permissions plugin used: " + permsmanager + " version " + permsversion + "\n");
					report.append("Bukkit version: " + getServer().getVersion() + "\n");
					report.append("Java version: " + System.getProperty("java.version") + " " + System.getProperty("java.vendor") + "\n");
					report.append("Operating system: " + System.getProperty("os.name") + " " + System.getProperty("os.version") + " " + System.getProperty("os.arch") + "\n\n");
					report.append("Plugins: \n");
					for(Plugin p : Bukkit.getPluginManager().getPlugins()) {
						report.append(p.getName() + " version " + p.getDescription().getVersion() + "\n");
					}
					report.append("\nWorlds: \n");
					for(World world : getServer().getWorlds()) {
						report.append(world.getName() + "\n");
					}
					ReportMakerThread rmthread = new ReportMakerThread(this, report, sender);
					Thread dispatchThread = new Thread(rmthread);
		            dispatchThread.start();
		            return true;
				}else if(args[0].equalsIgnoreCase("debug")) {
					if(!sender.hasPermission("enjin.debug")) {
						sender.sendMessage(ChatColor.RED + "You need to have the \"enjin.debug\" permission or OP to run that command!");
						return true;
					}
					if(debug) {
						debug = false;
					}else {
						debug = true;
					}
					sender.sendMessage(ChatColor.GREEN + "Debugging has been set to " + debug);
					return true;
				}
			}
		}
		return false;
	}
	
	public static void sendAddRank(final String world, final String group, final String player) {
		exec.submit(
			new Runnable() {
				@Override
				public void run() {
					try {
						sendAPIQuery("minecraft-set-rank", "authkey=" + hash, "world=" + world, "player=" + player, "group=" + group);
					} catch (Throwable t) {
						Bukkit.getLogger().warning("[Enjin Minecraft Plugin] There was an error synchronizing group " + group + ", for user " + player + ".");
						t.printStackTrace();
					}
				}
			}
		);
	}
	
	public static void sendRemoveRank(final String world, final String group, final String player) {
		exec.submit(
			new Runnable() {
				@Override
				public void run() {
					try {
						sendAPIQuery("minecraft-remove-rank", "authkey=" + hash, "world=" + world, "player=" + player, "group=" + group);
					} catch (Throwable t) {
						Bukkit.getLogger().warning("[Enjin Minecraft Plugin] There was an error synchronizing group " + group + ", for user " + player + ".");
						t.printStackTrace();
					}
				}
			}
		);
	}
	
	public static boolean keyValid(boolean save, String key) {
		try {
			if(key == null) {
				return false;
			}
			if(key.length() < 2) {
				return false;
			}
			if(save) {
				return sendAPIQuery("minecraft-auth", "key=" + key, "port=" + minecraftport, "save=1"); //save
			} else {
				return sendAPIQuery("minecraft-auth", "key=" + key, "port=" + minecraftport); //just check info
			}
		} catch (Throwable t) {
			Bukkit.getLogger().warning("[Enjin Minecraft Plugin] There was an error synchronizing game data to the enjin server.");
			t.printStackTrace();
			return false;
		}
	}
	
	public static boolean sendAPIQuery(String urls, String... queryValues) throws MalformedURLException {
		URL url = new URL((usingSSL ? "https" : "http") + "://api.enjin.com/api/" + urls);
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
				return true;
			}
			return false;
		} catch (SSLHandshakeException e) {
			Bukkit.getLogger().warning("[Enjin Minecraft Plugin] SSLHandshakeException, The plugin will use http without SSL. This may be less secure.");
			usingSSL = false;
			return sendAPIQuery(urls, queryValues);
		} catch (SocketTimeoutException e) {
			Bukkit.getLogger().warning("[Enjin Minecraft Plugin] Timeout, the enjin server didn't respond within the required time. Please be patient and report this bug to enjin.");
			return false;
		} catch (Throwable t) {
			t.printStackTrace();
			Bukkit.getLogger().warning("[Enjin Minecraft Plugin] Failed to send query to enjin server! " + t.getClass().getName() + ". Data: " + url + "?" + query.toString());
			return false;
		}
	}
	
	public static synchronized void setHash(String hash) {
		EnjinMinecraftPlugin.hash = hash;
	}
	
	public static synchronized String getHash() {
		return EnjinMinecraftPlugin.hash;
	}
	
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
        	Bukkit.getPluginManager().registerEvents(new bPermsChangeListener(this), this);
        	return;
        }
        Plugin groupmanager = this.getServer().getPluginManager().getPlugin("GroupManager");
        if(groupmanager != null) {
        	this.groupmanager = (GroupManager)groupmanager;
            debug("GroupManager found, hooking custom events.");
        	Bukkit.getPluginManager().registerEvents(new GroupManagerListener(this), this);
        	return;
        }
        debug("No suitable permissions plugin found, falling back to synching on player disconnect.");
        debug("You might want to switch to PermissionsEx, bPermissions, or Essentials GroupManager.");
        
	}
}
