package com.enjin.officialplugin.threaded;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.Proxy;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Set;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import com.enjin.officialplugin.EnjinErrorReport;
import com.enjin.officialplugin.EnjinMinecraftPlugin;
import com.enjin.officialplugin.packets.Packet10AddPlayerGroup;
import com.enjin.officialplugin.packets.Packet11RemovePlayerGroup;
import com.enjin.officialplugin.packets.Packet12ExecuteCommand;
import com.enjin.officialplugin.packets.Packet13ExecuteCommandAsPlayer;
import com.enjin.officialplugin.packets.Packet14NewerVersion;
import com.enjin.officialplugin.packets.Packet17AddWhitelistPlayers;
import com.enjin.officialplugin.packets.Packet18RemovePlayersFromWhitelist;
import com.enjin.officialplugin.packets.PacketUtilities;

/**
 * 
 * @author OverCaste (Enjin LTE PTD).
 * This software is released under an Open Source license.
 * @copyright Enjin 2012.
 * 
 */

public class PeriodicVoteTask implements Runnable {
	
	EnjinMinecraftPlugin plugin;
	ConcurrentHashMap<String, String> removedplayervotes = new ConcurrentHashMap<String, String>();
	int numoffailedtries = 0;
	
	public PeriodicVoteTask(EnjinMinecraftPlugin plugin) {
		this.plugin = plugin;
	}
	private URL getUrl() throws Throwable {
		return new URL((EnjinMinecraftPlugin.usingSSL ? "https" : "http") + EnjinMinecraftPlugin.apiurl + "minecraft-votifier");
	}
	
	@Override
	public void run() {
		//Only run if we have votes to send.
		if(plugin.playervotes.size() > 0) {

			boolean successful = false;
			StringBuilder builder = new StringBuilder();
			try {
				plugin.debug("Connecting to Enjin to send votes...");
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
				con.setReadTimeout(15000);
				con.setConnectTimeout(15000);
				con.setDoInput(true);
				con.setDoOutput(true);
				con.setRequestProperty("User-Agent", "Mozilla/4.0");
				con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
				//StringBuilder builder = new StringBuilder();
				builder.append("authkey=" + encode(EnjinMinecraftPlugin.hash));
				builder.append("&votifier=" + encode(getVotes()));
				builder.append("&accepts-packets=true");
				con.setRequestProperty("Content-Length", String.valueOf(builder.length()));
				plugin.debug("Sending content: \n" + builder.toString());
				con.getOutputStream().write(builder.toString().getBytes());
				//System.out.println("Getting input stream...");
				InputStream in = con.getInputStream();
				String success = handleInput(in);
				//System.out.println("Handling input stream...");
				if(success.equalsIgnoreCase("ok")) {
					successful = true;
					if(plugin.unabletocontactenjin) {
						plugin.unabletocontactenjin = false;
						Player[] players = plugin.getServer().getOnlinePlayers();
						for(Player player : players) {
							if(player.hasPermission("enjin.notify.connectionstatus")) {
								player.sendMessage(ChatColor.DARK_GREEN + "[Enjin Minecraft Plugin] Connection to Enjin re-established!");
								plugin.getLogger().info("Connection to Enjin re-established!");
							}
						}
					}
				}else if(success.equalsIgnoreCase("auth_error")) {
					plugin.authkeyinvalid = true;
					EnjinMinecraftPlugin.enjinlogger.warning("[Enjin Minecraft Plugin] Auth key invalid. Please regenerate on the enjin control panel.");
					plugin.getLogger().warning("Auth key invalid. Please regenerate on the enjin control panel.");
					plugin.stopTask();
					plugin.unregisterEvents();
					Player[] players = plugin.getServer().getOnlinePlayers();
					for(Player player : players) {
						if(player.hasPermission("enjin.notify.invalidauthkey")) {
							player.sendMessage(ChatColor.DARK_RED + "[Enjin Minecraft Plugin] Auth key is invalid. Please generate a new one.");
						}
					}
					successful = false;
				}else if(success.equalsIgnoreCase("bad_data")) {
					EnjinMinecraftPlugin.enjinlogger.warning("[Enjin Minecraft Plugin] Oops, we sent bad data, please send the enjin.log file to enjin to debug.");
					plugin.getLogger().warning("Oops, we sent bad data, please send the enjin.log file to enjin to debug.");
					successful = false;
				}else if(success.equalsIgnoreCase("retry_later")) {
					EnjinMinecraftPlugin.enjinlogger.info("[Enjin Minecraft Plugin] Enjin said to wait, saving data for next sync.");
					plugin.getLogger().info("Enjin said to wait, saving data for next sync.");
					successful = false;
				}else if(success.equalsIgnoreCase("connect_error")) {
					EnjinMinecraftPlugin.enjinlogger.info("[Enjin Minecraft Plugin] Enjin is having something going on, if you continue to see this error please report it to enjin.");
					plugin.getLogger().info("Enjin is having something going on, if you continue to see this error please report it to enjin.");
					successful = false;
				}else {
					EnjinMinecraftPlugin.enjinlogger.info("[Enjin Minecraft Plugin] Something happened on sync, if you continue to see this error please report it to enjin.");
					EnjinMinecraftPlugin.enjinlogger.info("Response code: " + success);
					plugin.getLogger().info("Something happened on sync, if you continue to see this error please report it to enjin.");
					plugin.getLogger().info("Response code: " + success);
					successful = false;
				}
			} catch (SocketTimeoutException e) {
				//We don't need to spam the console every minute if the synch didn't complete correctly.
				if(numoffailedtries++ > 5) {
					EnjinMinecraftPlugin.enjinlogger.warning("[Enjin Minecraft Plugin] Timeout, the enjin server didn't respond within the required time. Please be patient and report this bug to enjin.");
					Bukkit.getLogger().warning("[Enjin Minecraft Plugin] Timeout, the enjin server didn't respond within the required time. Please be patient and report this bug to enjin.");
					numoffailedtries = 0;
				}
				plugin.lasterror = new EnjinErrorReport(e, "Regular synch. Information sent:\n" + builder.toString());
			} catch (Throwable t) {
				//We don't need to spam the console every minute if the synch didn't complete correctly.
				if(numoffailedtries++ > 30) {
					EnjinMinecraftPlugin.enjinlogger.warning("[Enjin Minecraft Plugin] Oops, we didn't get a proper response, we may be doing some maintenance. Please be patient and report this bug to enjin if it persists.");
					Bukkit.getLogger().warning("[Enjin Minecraft Plugin] Oops, we didn't get a proper response, we may be doing some maintenance. Please be patient and report this bug to enjin if it persists.");
					numoffailedtries = 0;
				}
				if(plugin.debug) {
					t.printStackTrace();
				}
				plugin.lasterror = new EnjinErrorReport(t, "Votifier sync. Information sent:\n" + builder.toString());
				EnjinMinecraftPlugin.enjinlogger.warning(plugin.lasterror.toString());
			}
			if(!successful) {
				plugin.debug("Vote synch unsuccessful.");
				
				Set<Entry<String, String>> voteset = removedplayervotes.entrySet();
				for(Entry<String, String> entry : voteset) {
					//If the plugin has put new votes in, let's not overwrite them.
					if(plugin.playervotes.containsKey(entry.getKey())) {
						//combine the lists.
						String lists = plugin.playervotes.get(entry.getKey()) + "," + entry.getValue();
						plugin.playervotes.put(entry.getKey(), lists);
					}else {
						plugin.playervotes.put(entry.getKey(), entry.getValue());
					}
					removedplayervotes.remove(entry.getKey());
				}
			}else {
				plugin.debug("Vote synch successful.");
			}
		}
	}
	
	private String getVotes() {
		removedplayervotes.clear();
		StringBuilder votes = new StringBuilder();
		Set<Entry<String,String>> voteset = plugin.playervotes.entrySet();
		for(Entry<String, String> entry : voteset) {
			String player = entry.getKey();
			String lists = entry.getValue();
			if(votes.length() != 0) {
				votes.append(";");
			}
			votes.append(player + ":" + lists);
			removedplayervotes.put(player, lists);
			plugin.playervotes.remove(player);
		}
		return votes.toString();
	}
	private String encode(String in) throws UnsupportedEncodingException {
		return URLEncoder.encode(in, "UTF-8");
		//return in;
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
	private String handleInput(InputStream in) throws IOException {
		String tresult = "Unknown Error";
		BufferedInputStream bin = new BufferedInputStream(in);
		bin.mark(Integer.MAX_VALUE);
		//TODO: A for loop??? Maybe a while(code = in.read() != -1) {}
		for(;;) {
			int code = bin.read();
			switch(code) {
			case -1:
				plugin.debug("No more packets. End of stream. Update ended.");
				bin.reset();
				StringBuilder input = new StringBuilder();
				while((code = bin.read()) != -1) {
					input.append((char)code);
				}
				plugin.debug("Raw data received:\n" + input.toString());
				return tresult; //end of stream reached
			case 0x10:
				plugin.debug("Packet [0x10](Add Player Group) received.");
				Packet10AddPlayerGroup.handle(bin, plugin);
				break;
			case 0x11:
				plugin.debug("Packet [0x11](Remove Player Group) received.");
				Packet11RemovePlayerGroup.handle(bin, plugin);
				break;
			case 0x12:
				plugin.debug("Packet [0x12](Execute Command) received.");
				Packet12ExecuteCommand.handle(bin, plugin);
				break;
			case 0x13:
				plugin.debug("Packet [0x13](Execute command as Player) received.");
				Packet13ExecuteCommandAsPlayer.handle(bin, plugin);
				break;
			case 0x0A:
				plugin.debug("Packet [0x0A](New Line) received, ignoring...");
				break;
			case 0x0D:
				plugin.debug("Packet [0x0D](Carriage Return) received, ignoring...");
				break;
			case 0x14:
				plugin.debug("Packet [0x14](Newer Version) received.");
				Packet14NewerVersion.handle(bin, plugin);
				break;
			case 0x17:
				plugin.debug("Packet [0x17](Add Whitelist Players) received.");
				Packet17AddWhitelistPlayers.handle(bin, plugin);
				break;
			case 0x18:
				plugin.debug("Packet [0x18](Remove Players From Whitelist) received.");
				Packet18RemovePlayersFromWhitelist.handle(bin, plugin);
				break;
			case 0x19:
				plugin.debug("Packet [0x20](Enjin Status) received.");
				tresult = PacketUtilities.readString(bin);
				break;
			default :
				plugin.getLogger().warning("[Enjin] Received an invalid opcode: " + code);
				EnjinMinecraftPlugin.enjinlogger.warning("[Enjin] Received an invalid opcode: " + code);
			}
		}
	}
}
