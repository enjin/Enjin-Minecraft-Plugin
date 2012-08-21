package com.enjin.officialplugin;

import java.io.*;
import java.net.*;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

import javax.net.ssl.HttpsURLConnection;

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
	
	@Override
	public void onEnable() {
		try {
			initVariables();
			initFiles();
			initPlugins();
			usingGroupManager = (permission instanceof Permission_GroupManager);
			if(keyValid(false, hash)) {
				startTask();
				registerEvents();
			} else {
				Bukkit.getLogger().warning("The specified key is invalid, please enter the right one with /enjinkey");
			}
		}
		catch(Throwable t) {
			Bukkit.getLogger().warning("Couldn't enable EnjinMinecraftPlugin! Reason: " + t.getMessage());
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
			throw new Exception("Couldn't find a localhost ip! Please report this problem!");
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
		Bukkit.getScheduler().scheduleAsyncRepeatingTask(this, task, 600L, 600L);
	}
	
	private void registerEvents() {
		Bukkit.getPluginManager().registerEvents(listener, this);
	}
	
	private void stopTask() {
		Bukkit.getScheduler().cancelTasks(this);
	}
	
	private void unregisterEvents() {
		HandlerList.unregisterAll(listener);
	}
	
	private void initPlugins() throws Throwable {
		if(!Bukkit.getPluginManager().isPluginEnabled("Vault")) {
			throw new Exception("Couldn't find the vault plugin! Please get it from dev.bukkit.org!");
		}
		initPermissions();
	}
	
	private void initPermissions() throws Throwable {
		RegisteredServiceProvider<Permission> provider = Bukkit.getServicesManager().getRegistration(Permission.class);
		if(provider == null) {
			Bukkit.getLogger().warning("Couldn't find a vault compatible permission plugin! Please install one before using the Enjin Minecraft Plugin.");
			return;
		}
		permission = provider.getProvider();
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
						if(!sendAPIQuery("minecraft-set-rank", "authkey=" + hash, "world=" + world, "player=" + player, "group=" + group)) {
							throw new Exception("Received 'false' from the enjin data server!");
						}
					} catch (Throwable t) {
						Bukkit.getLogger().warning("There was an error synchronizing group " + group + ", for user " + player + ".");
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
						if(!sendAPIQuery("minecraft-remove-rank", "authkey=" + hash, "world=" + world, "player=" + player, "group=" + group)) {
							throw new Exception("Received 'false' from the enjin data server!");
						}
					} catch (Throwable t) {
						Bukkit.getLogger().warning("There was an error synchronizing group " + group + ", for user " + player + ".");
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
			Bukkit.getLogger().warning("There was an error synchronizing game data to the enjin server.");
			t.printStackTrace();
			return false;
		}
	}
	
	public static boolean sendAPIQuery(String urls, String... queryValues) throws MalformedURLException {
		URL url = new URL("https://api.enjin.com/api/" + urls);
		StringBuilder query = new StringBuilder();
		try {
			HttpsURLConnection con = (HttpsURLConnection) url.openConnection();
			con.setRequestMethod("GET");
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
		} catch (Throwable t) {
			t.printStackTrace();
			Bukkit.getLogger().warning("Failed to send query to enjin server! " + t.getClass().getName() + ". Data: " + url + "?" + query.toString());
			return false;
		}
	}
}
