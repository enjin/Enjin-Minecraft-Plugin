package com.enjin.officialplugin;

import java.io.*;
import java.net.*;
import java.util.Properties;
import java.util.logging.Logger;

import net.milkbowl.vault.permission.Permission;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Server;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * 
 * @author ikillforeyou (Enjin LTE PTD).
 * This software is released under an Open Source license.
 * @copyright Enjin 2012.
 * 
 */

public class EnjinMinecraftPlugin extends JavaPlugin {
	File hashFile;
	static String hash = "";
	Server s;
	Logger logger;
	static Permission permission = null;
	
	Thread listenThread;
	static ServerConnectionManager conManager;
	
	final EMPListener listener = new EMPListener();
	
	static InetAddress localip;
	static String minecraftport;
	static String minecraftip;
	
	@Override
	public void onEnable() {
		try {
			initVariables();
			initFiles();
			initPlugins();
			Bukkit.getPluginManager().registerEvents(listener, this);
			listenThread.start();
		}
		catch(Throwable t) {
			Bukkit.getLogger().warning("Couldn't enable EnjinMinecraftPlugin! Reason: " + t.getMessage());
			t.printStackTrace();
			this.setEnabled(false);
		}
	}
	
	@Override
	public void onDisable() {
		conManager.stop();
	}
	
	private void initVariables() throws Throwable {
		hashFile = new File(this.getDataFolder(), "HASH.txt");
		s = Bukkit.getServer();
		logger = Bukkit.getLogger();
		conManager = new ServerConnectionManager();
		listenThread = new Thread(conManager);
		try {
			Properties serverProperties = new Properties();
			FileInputStream in = new FileInputStream(new File("server.properties"));
			serverProperties.load(in);
			in.close();
			minecraftip = serverProperties.getProperty("server-ip");
			minecraftport = serverProperties.getProperty("server-port");
			if(minecraftip == null || minecraftip.equals("")) {
				minecraftip = InetAddress.getLocalHost().getHostAddress();
				localip = null;
			} else {
				localip = InetAddress.getByName(minecraftip);
			}
		} catch (Throwable t) {
			localip = null;
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
	
	/*private String generateHash() throws Throwable {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		if(localip != null) {
			try {
				NetworkInterface ni = NetworkInterface.getByInetAddress(localip);
				out.write(ni.getHardwareAddress());
			} catch (Throwable t) {
				//vpn
			}
		}
		out.write(ByteBuffer.allocate(8).putLong(System.currentTimeMillis()).array());
		out.write(ByteBuffer.allocate(8).putLong(new Random(System.currentTimeMillis()-1125).nextLong()).array());
		MessageDigest hasher = MessageDigest.getInstance("SHA-256");
		byte[] hashBytes = hasher.digest(out.toByteArray());
		out.close();
		StringBuffer ret = new StringBuffer();
	    for (int i=0;i<hashBytes.length;i++) {
	    	String hex=Integer.toHexString(0xff & hashBytes[i]);
	   	    if(hex.length()==1) {
	   	    	ret.append('0');
	   	    }
	   	    ret.append(hex);
	    }
	    return ret.toString();
	}*/
	
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
			hash = args[0];
			try {
				BufferedWriter writer = new BufferedWriter(new FileWriter(hashFile));
				writer.write(hash);
				writer.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			sendKeyUpdate(hash);
			sender.sendMessage(ChatColor.GREEN + "Set the enjin key to " + hash);
			return true;
		}
		return false;
	}
	
	public static void sendAddRank(String world, String group, String player) {
		try {
			if(!sendAPIQuery("https://api.enjin.com/api/minecraft-set-rank", "authkey=" + hash, "world=" + world, "player=" + player, "group=" + group)) {
				throw new Exception("Received 'false' from the enjin data server!");
			}
		} catch (Throwable t) {
			Bukkit.getLogger().warning("There was an error synchronizing group " + group + ", for user " + player + ".");
			t.printStackTrace();
		}
	}
	
	public static void sendRemoveRank(String world, String group, String player) {
		try {
			if(!sendAPIQuery("https://api.enjin.com/api/minecraft-remove-rank", "authkey=" + hash, "world=" + world, "player=" + player, "group=" + group)) {
				throw new Exception("Received 'false' from the enjin data server!");
			}
		} catch (Throwable t) {
			Bukkit.getLogger().warning("There was an error synchronizing group " + group + ", for user " + player + ".");
			t.printStackTrace();
		}
	}
	
	public static void sendKeyUpdate(String key) {
		try {
			if(!sendAPIQuery("https://api.enjin.com/api/minecraft-auth", "key=" + key, "host=" + minecraftip, "port=" + minecraftport)) {
				throw new Exception("Received 'false' from the enjin data server!");
			}
		} catch (Throwable t) {
			Bukkit.getLogger().warning("There was an error synchronizing game data to the enjin server.");
			t.printStackTrace();
		}
	}
	
	public static boolean sendAPIQuery(String urls, String... queryValues) throws MalformedURLException {
		URL url = new URL(urls);
		try {
			URLConnection con = url.openConnection();
			con.setDoInput(true);
			con.setDoOutput(true);
			StringBuilder query = new StringBuilder();
			for(String val : queryValues) {
				query.append('&');
				query.append(val);
			}
			if(queryValues.length > 0) {
				query.deleteCharAt(0); //remove first &
			}
			con.setRequestProperty("Content-length", String.valueOf(query.length()));
			con.getOutputStream().write(query.toString().getBytes());
			return (con.getInputStream().read() == '1');
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
	}
}
