package com.enjin.officialplugin;

import java.io.*;
import java.net.*;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

import javax.net.ssl.SSLHandshakeException;

import net.milkbowl.vault.permission.Permission;
import net.milkbowl.vault.permission.plugins.Permission_GroupManager;

import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * 
 * @author OverCaste (Enjin LTE PTD).
 * This software is released under an Open Source license.
 * @copyright Enjin 2012.
 * 
 */

public class EnjinMinecraftPlugin extends JavaPlugin {
	public static boolean usingGroupManager = false;
	File hashFile;
	static String hash = "";
	Server s;
	Logger logger;
	static Permission permission = null;
	
	final EMPListener listener = new EMPListener();
	final PeriodicEnjinTask task = new PeriodicEnjinTask();
	static final ExecutorService exec = Executors.newCachedThreadPool();
	static String minecraftport;
	static boolean usingSSL = true;
	
	private void debug(String s) {
		//System.out.println("Enjin Debug: " + s);
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
			usingGroupManager = (permission instanceof Permission_GroupManager);
			debug("Checking key valid.");
			if(keyValid(false, hash)) {
				debug("Key valid.");
				startTask();
				registerEvents();
			} else {
				Bukkit.getLogger().warning("[Enjin Minecraft Plugin] Failed to authenticate with enjin! This may mean your key is invalid, or there was an error connecting.");
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
		unregisterEvents();
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
	
	private void initFiles() throws Throwable {
		File parent;
		parent = hashFile.getParentFile();
		if(parent != null) {
			parent.mkdirs();
		}
		if(!hashFile.exists()) {
			hashFile.createNewFile();
		} else {
			BufferedReader r = new BufferedReader(new FileReader(hashFile));
			hash = r.readLine();
		}
	}
	
	private void startTask() {
		debug("Starting task.");
		Bukkit.getScheduler().scheduleAsyncRepeatingTask(this, task, 1200L, 1200L);
	}
	
	private void registerEvents() {
		debug("Registering events.");
		Bukkit.getPluginManager().registerEvents(listener, this);
	}
	
	private void stopTask() {
		debug("Stopping task.");
		Bukkit.getScheduler().cancelTasks(this);
	}
	
	private void unregisterEvents() {
		debug("Unregistering events.");
		HandlerList.unregisterAll(listener);
	}
	
	private void initPlugins() throws Throwable {
		if(!Bukkit.getPluginManager().isPluginEnabled("Vault")) {
			throw new Exception("[Enjin Minecraft Plugin] Couldn't find the vault plugin! Please get it from dev.bukkit.org!");
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
			if(!sender.isOp()) {
				sender.sendMessage(ChatColor.RED + "You need to be an operator to run that command!");
				return true;
			}
			if(args.length != 1) {
				return false;
			}
			Bukkit.getLogger().info("Checking if key is valid");
			if(!keyValid(true, args[0])) {
				sender.sendMessage(ChatColor.RED + "That key is invalid! Make sure you've entered it properly!");
				stopTask();
				unregisterEvents();
				return true;
			}
			if(args[0].equals(hash)) {
				sender.sendMessage(ChatColor.YELLOW + "The speficied key and the existing one are the same!");
				return true;
			}
			hash = args[0];
			try {
				debug("Writing hash to file.");
				BufferedWriter writer = new BufferedWriter(new FileWriter(hashFile));
				writer.write(hash);
				writer.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			sender.sendMessage(ChatColor.GREEN + "Set the enjin key to " + hash);
			stopTask();
			unregisterEvents();
			startTask();
			registerEvents();
			return true;
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
}
