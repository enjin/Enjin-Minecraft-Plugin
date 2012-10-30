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

import com.enjin.officialplugin.EnjinErrorReport;
import com.enjin.officialplugin.EnjinMinecraftPlugin;

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
				con.setRequestProperty("Content-Length", String.valueOf(builder.length()));
				plugin.debug("Sending content: \n" + builder.toString());
				con.getOutputStream().write(builder.toString().getBytes());
				//System.out.println("Getting input stream...");
				InputStream in = con.getInputStream();
				//System.out.println("Handling input stream...");
				if(((char)in.read()) != '1') {
					successful = false;
				}else {
					successful = true;
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
}
