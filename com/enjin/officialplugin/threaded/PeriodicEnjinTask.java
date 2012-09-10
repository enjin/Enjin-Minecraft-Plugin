package com.enjin.officialplugin.threaded;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.Proxy;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Set;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import com.enjin.officialplugin.EnjinMinecraftPlugin;
import com.enjin.officialplugin.packets.Packet10AddPlayerGroup;
import com.enjin.officialplugin.packets.Packet11RemovePlayerGroup;
import com.enjin.officialplugin.packets.Packet12ExecuteCommand;
import com.enjin.officialplugin.packets.Packet13ExecuteCommandAsPlayer;
import com.enjin.officialplugin.packets.Packet14NewerVersion;

/**
 * 
 * @author OverCaste (Enjin LTE PTD).
 * This software is released under an Open Source license.
 * @copyright Enjin 2012.
 * 
 */

public class PeriodicEnjinTask implements Runnable {
	
	EnjinMinecraftPlugin plugin;
	ConcurrentHashMap<String, String> removedplayerperms = new ConcurrentHashMap<String, String>();
	int numoffailedtries = 0;
	
	public PeriodicEnjinTask(EnjinMinecraftPlugin plugin) {
		this.plugin = plugin;
	}
	private URL getUrl() throws Throwable {
		return new URL((EnjinMinecraftPlugin.usingSSL ? "https" : "http") + "://api.enjin.com/api/minecraft-sync");
	}
	
	@Override
	public void run() {
		boolean successful = false;
		try {
			plugin.debug("Connecting to Enjin...");
			URL enjinurl = getUrl();
			HttpURLConnection con;
			// Mineshafter creates a socks proxy, so we can safely bypass it
	        // It does not reroute POST requests so we need to go around it
	        if (isMineshafterPresent()) {
	            con = (HttpURLConnection) enjinurl.openConnection(Proxy.NO_PROXY);
	        } else {
	            con = (HttpURLConnection) enjinurl.openConnection();
	        }
			con.setRequestMethod("POST");
			con.setReadTimeout(3000);
			con.setConnectTimeout(3000);
			con.setDoInput(true);
			con.setDoOutput(true);
			con.setRequestProperty("User-Agent", "Mozilla/4.0");
			con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
			StringBuilder builder = new StringBuilder();
			builder.append("authkey=" + encode(EnjinMinecraftPlugin.hash));
			builder.append("&maxplayers=" + encode(String.valueOf(Bukkit.getServer().getMaxPlayers()))); //max players
			builder.append("&players=" + encode(String.valueOf(Bukkit.getServer().getOnlinePlayers().length))); //current players
			builder.append("&hasranks=" + encode(((EnjinMinecraftPlugin.permission == null) ? "FALSE" : "TRUE")));
			builder.append("&pluginversion=" + encode(plugin.getDescription().getVersion()));
			builder.append("&plugins=" + encode(getPlugins()));
			builder.append("&playerlist=" + encode(getPlayers()));
			builder.append("&groups=" + encode(getGroups()));
			builder.append("&worlds=" + encode(getWorlds()));
			if(plugin.playerperms.size() > 0) {
				builder.append("&playergroups=" + encode(getPlayerGroups()));
			}
			con.setRequestProperty("Content-Length", String.valueOf(builder.length()));
			plugin.debug("Sending content: \n" + builder.toString());
			con.getOutputStream().write(builder.toString().getBytes());
			//System.out.println("Getting input stream...");
			InputStream in = con.getInputStream();
			//System.out.println("Handling input stream...");
			handleInput(in);
			successful = true;
		} catch (SocketTimeoutException e) {
			//We don't need to spam the console every minute if the synch didn't complete correctly.
			if(numoffailedtries++ > 5) {
				Bukkit.getLogger().warning("[Enjin Minecraft Plugin] Timeout, the enjin server didn't respond within the required time. Please be patient and report this bug to enjin.");
				numoffailedtries = 0;
			}
		} catch (Throwable t) {
			//We don't need to spam the console every minute if the synch didn't complete correctly.
			if(numoffailedtries++ > 5) {
				Bukkit.getLogger().warning("[Enjin Minecraft Plugin] Oops, we didn't get a proper response, we may be doing some maintenance. Please be patient and report this bug to enjin if it persists.");
				numoffailedtries = 0;
			}
			if(plugin.debug) {
				t.printStackTrace();
			}
		}
		if(!successful) {
			plugin.debug("Synch unsuccessful.");
			Set<Entry<String, String>> es = removedplayerperms.entrySet();
			for(Entry<String, String> entry : es) {
				//If the plugin has put a new set of player perms in for this player,
				//let's not overwrite it.
				if(!plugin.playerperms.containsKey(entry.getKey())) {
					plugin.playerperms.put(entry.getKey(), entry.getValue());
				}
				removedplayerperms.remove(entry.getKey());
			}
		}else {
			plugin.debug("Synch successful.");
		}
	}
	
	private String getPlayerGroups() {
		removedplayerperms.clear();
		HashMap<String, String> theperms = new HashMap<String, String>();
		Set<Entry<String, String>> es = plugin.playerperms.entrySet();
		for(Entry<String, String> entry : es) {
			StringBuilder perms = new StringBuilder();
			/*
			if(theperms.containsKey(entry.getKey().getPlayerName())) {
				perms.append(theperms.get(entry.getKey().getPlayerName()));
			}*/
			//Let's get global groups
			String[] tempperms = EnjinMinecraftPlugin.permission.getPlayerGroups((World)null, entry.getKey());
			if(perms.length() > 0 && tempperms.length > 0) {
				perms.append("|");
			}
			LinkedList<String> globalperms = new LinkedList<String>();
			if(tempperms != null && tempperms.length > 0) {
				perms.append('*' + ":");
				for(int i = 0, j = 0; i < tempperms.length; i++) {
					if(EnjinMinecraftPlugin.usingGroupManager && tempperms[i].startsWith("g:")) {
						continue;
					}
					if(j > 0) {
						perms.append(",");
					}
					globalperms.add(tempperms[i]);
					perms.append(tempperms[i]);
					j++;
				}
			}
			//Now let's get groups per world.
			for(World w: Bukkit.getWorlds()) {
				tempperms = EnjinMinecraftPlugin.permission.getPlayerGroups(w, entry.getKey());
				if(tempperms != null && tempperms.length > 0) {
					LinkedList<String> worldperms = new LinkedList<String>();
					for(int i = 0; i < tempperms.length; i++) {
						if(globalperms.contains(tempperms[i]) || (EnjinMinecraftPlugin.usingGroupManager && tempperms[i].startsWith("g:"))) {
							continue;
						}
						worldperms.add(tempperms[i]);
					}
					if(perms.length() > 0 && worldperms.size() > 0) {
						perms.append("|");
					}
					if(worldperms.size() > 0) {
						perms.append(w.getName() + ":");
						for(int i = 0; i < worldperms.size(); i++) {
							if(i > 0) {
								perms.append(",");
							}
							perms.append(worldperms.get(i));
						}
					}
				}
			}
			theperms.put(entry.getKey(), perms.toString());
			//remove that player from the list.
			plugin.playerperms.remove(entry.getKey());
			//If the synch fails we need to put the values back...
			removedplayerperms.put(entry.getKey(), entry.getValue());
		}
		StringBuilder allperms = new StringBuilder();
		Set<Entry<String, String>> ns = theperms.entrySet();
		for(Entry<String, String> entry : ns) {
			if(allperms.length() > 0) {
				allperms.append("\n");
			}
			allperms.append(entry.getKey() + ";" + entry.getValue());
		}
		return allperms.toString();
	}
	private String encode(String in) throws UnsupportedEncodingException {
		return URLEncoder.encode(in, "UTF-8");
		//return in;
	}
	
	private void handleInput(InputStream in) throws IOException {
		//TODO: A for loop??? Maybe a while(code = in.read() != -1) {}
		for(;;) {
			int code = in.read();
			switch(code) {
			case -1:
				return; //end of stream reached
			case 0x10:
				Packet10AddPlayerGroup.handle(in, plugin);
				break;
			case 0x11:
				Packet11RemovePlayerGroup.handle(in);
				break;
			case 0x12:
				Packet12ExecuteCommand.handle(in, plugin);
				break;
			case 0x13:
			case 0x0D:
				Packet13ExecuteCommandAsPlayer.handle(in, plugin);
				break;
			case 0x14:
				Packet14NewerVersion.handle(in, plugin);
				break;
			default :
				Bukkit.getLogger().warning("[Enjin] Received an invalid opcode: " + code);
			}
		}
	}

	private String getPlugins() {
		StringBuilder builder = new StringBuilder();
		for(Plugin p : Bukkit.getPluginManager().getPlugins()) {
			builder.append(',');
			builder.append(p.getName());
		}
		if(builder.length() > 2) {
			builder.deleteCharAt(0);
		}
		return builder.toString();
	}
	
	private String getPlayers() {
		StringBuilder builder = new StringBuilder();
		for(Player p : Bukkit.getOnlinePlayers()) {
			builder.append(',');
			builder.append(p.getName());
		}
		if(builder.length() > 2) {
			builder.deleteCharAt(0);
		}
		return builder.toString();
	}
	
	private String getGroups() {
		StringBuilder builder = new StringBuilder();
		if(EnjinMinecraftPlugin.usingGroupManager) {
			for(String group : EnjinMinecraftPlugin.permission.getGroups()) {
				builder.append(',');
				builder.append(group);
			}
		} else {
			for(String group : EnjinMinecraftPlugin.permission.getGroups()) {
				if(group.startsWith("g:")) {
					continue;
				}
				builder.append(',');
				builder.append(group);
			}
		}
		if(builder.length() > 2) {
			builder.deleteCharAt(0);
		}
		return builder.toString();
	}
	
	private String getWorlds() {
		StringBuilder builder = new StringBuilder();
		for(World w : Bukkit.getWorlds()) {
			builder.append(',');
			builder.append(w.getName());
		}
		if(builder.length() > 2) {
			builder.deleteCharAt(0);
		}
		return builder.toString();
	}
	/**
	 * Check if mineshafter is present. If it is, we need to bypass it to send POST requests
	 *
	 * @return
	 */
	private boolean isMineshafterPresent() {
	    try {
	        Class.forName("mineshafter.MineServer");
	        return true;
	    } catch (Exception e) {
	        return false;
	    }
	}
}
