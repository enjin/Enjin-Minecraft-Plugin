package com.enjin.officialplugin;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLEncoder;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

/**
 * 
 * @author OverCaste (Enjin LTE PTD).
 * This software is released under an Open Source license.
 * @copyright Enjin 2012.
 * 
 */

public class PeriodicEnjinTask implements Runnable {
	private URL getUrl() throws Throwable {
		return new URL((EnjinMinecraftPlugin.usingSSL ? "https" : "http") + "://api.enjin.com/api/minecraft-sync");
	}
	
	@Override
	public void run() {
		try {
			HttpURLConnection con = (HttpURLConnection)getUrl().openConnection();
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
			builder.append("&pluginversion=" + encode(Bukkit.getPluginManager().getPlugin("Enjin Minecraft Plugin").getDescription().getVersion()));
			builder.append("&plugins=" + encode(getPlugins()));
			builder.append("&playerlist=" + encode(getPlayers()));
			builder.append("&groups=" + encode(getGroups()));
			builder.append("&worlds=" + encode(getWorlds()));
			con.setRequestProperty("Content-Length", String.valueOf(builder.length()));
			con.getOutputStream().write(builder.toString().getBytes());
			InputStream in = con.getInputStream();
			handleInput(in);
		} catch (SocketTimeoutException e) {
			Bukkit.getLogger().warning("[Enjin Minecraft Plugin] Timeout, the enjin server didn't respond within the required time. Please be patient and report this bug to enjin.");
		} catch (Throwable t) {
			t.printStackTrace();
		}
	}
	
	private String encode(String in) throws UnsupportedEncodingException {
		return URLEncoder.encode(in, "UTF-8");
		//return in;
	}
	
	private void handleInput(InputStream in) throws IOException {
		for(;;) {
			int code = in.read();
			switch(code) {
			case -1:
				return; //end of stream reached
			case 0x10:
				Packet10AddPlayerGroup.handle(in);
				break;
			case 0x11:
				Packet11RemovePlayerGroup.handle(in);
				break;
			case 0x12:
				Packet12ExecuteCommand.handle(in);
				break;
			case 0x13:
				Packet13ExecuteCommandAsPlayer.handle(in);
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
}
