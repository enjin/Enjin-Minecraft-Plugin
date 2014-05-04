package com.enjin.officialplugin.threaded;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.Proxy;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.zip.GZIPOutputStream;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import com.enjin.officialplugin.CommandWrapper;
import com.enjin.officialplugin.EnjinErrorReport;
import com.enjin.officialplugin.EnjinMinecraftPlugin;
import com.enjin.officialplugin.packets.Packet10AddPlayerGroup;
import com.enjin.officialplugin.packets.Packet11RemovePlayerGroup;
import com.enjin.officialplugin.packets.Packet12ExecuteCommand;
import com.enjin.officialplugin.packets.Packet13ExecuteCommandAsPlayer;
import com.enjin.officialplugin.packets.Packet14DummyHandler;
import com.enjin.officialplugin.packets.Packet14NewerVersion;
import com.enjin.officialplugin.packets.Packet15RemoteConfigUpdate;
import com.enjin.officialplugin.packets.Packet16MultiUserNotice;
import com.enjin.officialplugin.packets.Packet17AddWhitelistPlayers;
import com.enjin.officialplugin.packets.Packet18RemovePlayersFromWhitelist;
import com.enjin.officialplugin.packets.Packet1ABanPlayers;
import com.enjin.officialplugin.packets.Packet1BPardonPlayers;
import com.enjin.officialplugin.packets.Packet1DPlayerPurchase;
import com.enjin.officialplugin.packets.Packet1ECommandsReceived;
import com.enjin.officialplugin.packets.PacketUtilities;
import com.enjin.officialplugin.stats.WriteStats;
import com.sun.xml.internal.messaging.saaj.util.ByteOutputStream;

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
	ConcurrentHashMap<String, String> removedplayervotes = new ConcurrentHashMap<String, String>();
	HashMap<String, String> removedbans = new HashMap<String, String>();
	HashMap<String, String> removedpardons = new HashMap<String, String>();
	int numoffailedtries = 0;
	int statdelay = 0;
	int plugindelay = 60;
	boolean firstrun = true;
	
	public PeriodicEnjinTask(EnjinMinecraftPlugin plugin) {
		this.plugin = plugin;
	}
	
	private URL getUrl() throws Throwable {
		return new URL((EnjinMinecraftPlugin.usingSSL ? "https" : "http") + EnjinMinecraftPlugin.apiurl + "minecraft-sync");
	}
	
	@Override
	public void run() {
		//Only run the ssl test on first run.
		if(firstrun && EnjinMinecraftPlugin.usingSSL) {
			if(!plugin.testHTTPSconnection()) {
				EnjinMinecraftPlugin.usingSSL = false;
				plugin.getLogger().warning("SSL test connection failed, The plugin will use http without SSL. This may be less secure.");
				EnjinMinecraftPlugin.enjinlogger.warning("SSL test connection failed, The plugin will use http without SSL. This may be less secure.");
			}
		}
		boolean successful = false;
		StringBuilder builder = new StringBuilder();
		try {
			EnjinMinecraftPlugin.debug("Connecting to Enjin...");
			URL enjinurl = getUrl();
			HttpURLConnection con;
			// Mineshafter creates a socks proxy, so we can safely bypass it
	        // It does not reroute POST requests so we need to go around it
	        if (EnjinMinecraftPlugin.isMineshafterPresent()) {
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
			if(firstrun) {
				builder.append("&maxplayers=" + encode(String.valueOf(Bukkit.getServer().getMaxPlayers()))); //max players
				builder.append("&mc_version=" + encode(plugin.mcversion));
			}
			builder.append("&players=" + encode(String.valueOf(Bukkit.getServer().getOnlinePlayers().length))); //current players
			builder.append("&hasranks=" + encode(((EnjinMinecraftPlugin.permission == null || EnjinMinecraftPlugin.permission.getName().equalsIgnoreCase("SuperPerms")) ? "FALSE" : "TRUE")));
			builder.append("&pluginversion=" + encode(plugin.getDescription().getVersion()));
			if(plugin.getCommandIDs().size() > 0) {
				builder.append("&executed_commands=" + encode(getExecutedCommands()));
			}
			//We only want to send the list of plugins every hour
			if(plugindelay++ >= 59) {
				builder.append("&plugins=" + encode(getPlugins()));
			}
			builder.append("&playerlist=" + encode(getPlayers()));
			builder.append("&worlds=" + encode(getWorlds()));
			builder.append("&tps=" + encode(getTPS()));
			builder.append("&time=" + encode(getTimes()));
			if(plugin.bannedplayers.size() > 0) {
				builder.append("&banned=" + encode(getBans()));
			}
			if(plugin.pardonedplayers.size() > 0) {
				builder.append("&unbanned=" + encode(getPardons()));
			}
			//Don't add the votifier tag if no one has voted.
			/* Votes are now handled in a separate thread.
			if(plugin.playervotes.size() > 0) {
				builder.append("&votifier=" + encode(getVotes()));
			}*/
			
			//We don't want to throw any errors if they are using superperms
			//which doesn't support groups. Therefore we can't support it.
			if(EnjinMinecraftPlugin.permission != null && !EnjinMinecraftPlugin.permission.getName().equalsIgnoreCase("SuperPerms")) {
				builder.append("&groups=" + encode(getGroups()));
				if(plugin.playerperms.size() > 0) {
					builder.append("&playergroups=" + encode(getPlayerGroups()));
				}
			}
			if(plugin.collectstats && (plugin.statssendinterval - 1) <= statdelay) {
				builder.append("&stats=" + encode(getStats()));
			}
			con.setRequestProperty("Content-Length", String.valueOf(builder.length()));
			EnjinMinecraftPlugin.debug("Sending content: \n" + builder.toString());
			con.getOutputStream().write(builder.toString().getBytes());
			//System.out.println("Getting input stream...");
			InputStream in = con.getInputStream();
			//System.out.println("Handling input stream...");
			String success = handleInput(in);
			//Schedule commands to execute regardless of success.
			Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, plugin.commandqueue);
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
				Player[] players = plugin.getServer().getOnlinePlayers();
				for(Player player : players) {
					if(player.hasPermission("enjin.notify.invalidauthkey")) {
						player.sendMessage(ChatColor.DARK_RED + "[Enjin Minecraft Plugin] Auth key is invalid. Please generate a new one.");
					}
				}
				successful = false;
			}else if(success.equalsIgnoreCase("bad_data")) {
				EnjinMinecraftPlugin.enjinlogger.warning("[Enjin Minecraft Plugin] Oops, we sent bad data, please send the enjin.log file to enjin to debug.");
				plugin.lasterror = new EnjinErrorReport("Enjin reported bad data", "Regular synch. Information sent:\n" + builder.toString());
				//plugin.getLogger().warning("Oops, we sent bad data, please send the enjin.log file to enjin to debug.");
				successful = false;
			}else if(success.equalsIgnoreCase("retry_later")) {
				EnjinMinecraftPlugin.enjinlogger.info("[Enjin Minecraft Plugin] Enjin said to wait, saving data for next sync.");
				//plugin.getLogger().info("Enjin said to wait, saving data for next sync.");
				successful = false;
			}else if(success.equalsIgnoreCase("connect_error")) {
				EnjinMinecraftPlugin.enjinlogger.info("[Enjin Minecraft Plugin] Enjin is having something going on, if you continue to see this error please report it to enjin.");
				plugin.lasterror = new EnjinErrorReport("Enjin said there's a connection error somewhere.", "Regular synch. Information sent:\n" + builder.toString());
				//plugin.getLogger().info("Enjin is having something going on, if you continue to see this error please report it to enjin.");
				successful = false;
			}else if(success.startsWith("invalid_op")) {
				plugin.lasterror = new EnjinErrorReport(success, "Regular synch. Information sent:\n" + builder.toString());
				successful = false;
			}else {
				EnjinMinecraftPlugin.enjinlogger.info("[Enjin Minecraft Plugin] Something happened on sync, if you continue to see this error please report it to enjin.");
				EnjinMinecraftPlugin.enjinlogger.info("Response code: " + success);
				plugin.getLogger().info("Something happened on sync, if you continue to see this error please report it to enjin.");
				plugin.getLogger().info("Response code: " + success);
				successful = false;
			}
			if(!successful) {
				if(numoffailedtries++ > 5 && !plugin.unabletocontactenjin) {
					numoffailedtries = 0;
					plugin.noEnjinConnectionEvent();
				}
			}else {
				//If the sync is successful let's reset the number of failed tries
				numoffailedtries = 0;
			}
		} catch (SocketTimeoutException e) {
			//We don't need to spam the console every minute if the synch didn't complete correctly.
			if(numoffailedtries++ > 5) {
				EnjinMinecraftPlugin.enjinlogger.warning("[Enjin Minecraft Plugin] Timeout, the enjin server didn't respond within the required time. Please be patient and report this bug to enjin.");
				plugin.getLogger().warning("Timeout, the enjin server didn't respond within the required time. Please be patient and report this bug to enjin.");
				numoffailedtries = 0;
				plugin.noEnjinConnectionEvent();
			}
			plugin.lasterror = new EnjinErrorReport(e, "Regular synch. Information sent:\n" + builder.toString());
		} catch (Throwable t) {
			//We don't need to spam the console every minute if the synch didn't complete correctly.
			if(numoffailedtries++ > 5) {
				EnjinMinecraftPlugin.enjinlogger.warning("[Enjin Minecraft Plugin] Oops, we didn't get a proper response, we may be doing some maintenance. Please be patient and report this bug to enjin if it persists.");
				plugin.getLogger().warning("Oops, we didn't get a proper response, we may be doing some maintenance. Please be patient and report this bug to enjin if it persists.");
				numoffailedtries = 0;
				plugin.noEnjinConnectionEvent();
			}
			if(EnjinMinecraftPlugin.debug) {
				t.printStackTrace();
			}
			plugin.lasterror = new EnjinErrorReport(t, "Regular synch. Information sent:\n" + builder.toString());
			EnjinMinecraftPlugin.enjinlogger.warning(plugin.lasterror.toString());
		}
		if(!successful) {
			EnjinMinecraftPlugin.debug("Synch unsuccessful.");
			statdelay++;
			Set<Entry<String, String>> es = removedplayerperms.entrySet();
			for(Entry<String, String> entry : es) {
				//If the plugin has put a new set of player perms in for this player,
				//let's not overwrite it.
				if(!plugin.playerperms.containsKey(entry.getKey())) {
					plugin.playerperms.put(entry.getKey(), entry.getValue());
				}
				removedplayerperms.remove(entry.getKey());
			}
			
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
			Set<Entry<String, String>> banset = removedbans.entrySet();
			for(Entry<String, String> entry : banset) {
				plugin.bannedplayers.put(entry.getKey(), entry.getValue());
			}
			banset.clear();
			Set<Entry<String, String>> pardonset = removedpardons.entrySet();
			for(Entry<String, String> entry : pardonset) {
				plugin.pardonedplayers.put(entry.getKey(), entry.getValue());
			}
			pardonset.clear();
		}else {
			firstrun = false;
			removedbans.clear();
			removedpardons.clear();
			EnjinMinecraftPlugin.debug("Synch successful.");
			if(plugin.collectstats && (plugin.statssendinterval - 1) <= statdelay) {
				statdelay = 0;
				//Let's remove the old stats...
				plugin.serverstats.reset();
				plugin.playerstats.clear();
			}else {
				statdelay++;
			}
			if(plugindelay >= 59) {
				plugindelay = 0;
			}
		}
		if(plugin.collectstats) {
			new WriteStats(plugin).write("stats.stats");
			EnjinMinecraftPlugin.debug("Stats saved to stats.stats.");
		}
	}
	
	private String getExecutedCommands() {
		StringBuilder sb = new StringBuilder();
		Iterator<Entry<String, CommandWrapper>> es = plugin.getCommandIDs().entrySet().iterator();
		while(es.hasNext() && sb.length() < 40*1024) {
			Entry<String, CommandWrapper> element = es.next();
			if(sb.length() > 0) {
				sb.append(",");
			}
			if(element.getValue().getResult().equals("")) {
				sb.append(element.getKey() + ":" + element.getValue().getHash());
			}else {
				sb.append(element.getKey() + ":" + element.getValue().getHash() + ":" + base64Encode(element.getValue().getResult()));
			}
		}
		return sb.toString();
		
	}

	private String getTPS() {
		return String.valueOf(plugin.tpstask.getTPSAverage());
	}
	private String getPardons() {
		StringBuilder pardons = new StringBuilder();
		Set<Entry<String, String>> pardonset = plugin.pardonedplayers.entrySet();
		for(Entry<String, String> pardon : pardonset) {
			if(pardons.length() > 0) {
				pardons.append(",");
			}
			if(pardon.getValue().equals("")) {
				pardons.append(pardon.getKey());
			}else {
				pardons.append(pardon.getValue() + ":" + pardon.getKey());
			}
			plugin.pardonedplayers.remove(pardon.getKey());
			removedpardons.put(pardon.getKey(), pardon.getValue());
		}
		return pardons.toString();
	}
	
	private String getBans() {
		StringBuilder bans = new StringBuilder();
		Set<Entry<String, String>> banset = plugin.bannedplayers.entrySet();
		for(Entry<String, String> ban : banset) {
			if(bans.length() > 0) {
				bans.append(",");
			}
			if(ban.getValue().equals("")) {
				bans.append(ban.getKey());
			}else {
				bans.append(ban.getValue() + ":" + ban.getKey());
			}
			plugin.bannedplayers.remove(ban.getKey());
			removedbans.put(ban.getKey(), ban.getValue());
		}
		return bans.toString();
	}
	
	private String getStats() {
		byte[] rawstats = new WriteStats(plugin).write();
		ByteOutputStream output = new ByteOutputStream();
		try {
			GZIPOutputStream out = new GZIPOutputStream(output);
			out.write(rawstats, 0, rawstats.length);
			out.finish();
			out.close();
			String serialized = javax.xml.bind.DatatypeConverter.printBase64Binary(output.getBytes());
			return serialized;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return "";
	}
	private String getPlayerGroups() {
		//We don't need to go any further if the permissions plugin is broken.
		if(plugin.permissionsnotworking) {
			return "";
		}
		removedplayerperms.clear();
		Iterator<Entry<String, String>> es = plugin.playerperms.entrySet().iterator();
		
		//With the push command, we need to limit how many ranks we send
		//with each synch.
		StringBuilder allperms = new StringBuilder();
		for(int k = 0; es.hasNext() && k < 3000 && allperms.length() < 30*1024; k++) {
			Entry<String, String> entry = es.next();
			StringBuilder perms = new StringBuilder();
			//Let's get global groups
			LinkedList<String> globalperms = new LinkedList<String>();
			//We don't want to get global groups with plugins that don't support it.
			try {
				if(plugin.supportsglobalgroups) {
					String[] tempperms = EnjinMinecraftPlugin.permission.getPlayerGroups((World)null, entry.getKey());
					if(perms.length() > 0 && tempperms.length > 0) {
						perms.append("|");
					}

					if(tempperms != null && tempperms.length > 0) {
						perms.append('*' + ":");
						for(int i = 0, j = 0; i < tempperms.length; i++) {
							if(j > 0) {
								perms.append(",");
							}
							globalperms.add(tempperms[i]);
							perms.append(tempperms[i]);
							j++;
						}
					}
				}
				//Now let's get groups per world.
				for(World w: Bukkit.getWorlds()) {
					String[] tempperms = EnjinMinecraftPlugin.permission.getPlayerGroups(w, entry.getKey());
					if(tempperms != null && tempperms.length > 0) {
						
						//The below variable is only used for GroupManager since it
						//likes transmitting all the groups
						LinkedList<String> skipgroups = new LinkedList<String>();
						
						LinkedList<String> worldperms = new LinkedList<String>();
						for(int i = 0; i < tempperms.length; i++) {
							//GroupManager has a bug where all lower groups in a hierarchy get
							//transmitted along with the main group, so we have to catch and
							//process it differently.
							if(plugin.groupmanager != null) {
								List<String> subgroups = plugin.groupmanager.getWorldsHolder().getWorldData(w.getName()).getGroup(tempperms[i]).getInherits();
								for(String group : subgroups) {
									//Groups are case insensitive in GM
									skipgroups.add(group.toLowerCase());
								}
							}
							if(globalperms.contains(tempperms[i]) || (EnjinMinecraftPlugin.usingGroupManager && (tempperms[i].startsWith("g:") || skipgroups.contains(tempperms[i].toLowerCase())))) {
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
				if(allperms.length() > 0) {
					allperms.append("\n");
				}
				allperms.append(entry.getKey() + ";" + perms.toString());
			}catch (Exception e) {
				EnjinMinecraftPlugin.debug("Unable to get permissions data for player " + entry.getKey());
				//Assume this plugin does not support offline players;
			}
			//remove that player from the list.
			plugin.playerperms.remove(entry.getKey());
			//If the synch fails we need to put the values back...
			removedplayerperms.put(entry.getKey(), entry.getValue());
		}
		return allperms.toString();
	}
	private String encode(String in) throws UnsupportedEncodingException {
		return URLEncoder.encode(in, "UTF-8");
		//return in;
	}
	
	public String handleInput(InputStream in) throws IOException {
		String tresult = "Unknown Error";
		BufferedInputStream bin = new BufferedInputStream(in);
		bin.mark(Integer.MAX_VALUE);
		boolean commandsrecieved = false;
		for(;;) {
			int code = bin.read();
			switch(code) {
			case -1:
				EnjinMinecraftPlugin.debug("No more packets. End of stream. Update ended.");
				bin.reset();
				StringBuilder input = new StringBuilder();
				while((code = bin.read()) != -1) {
					input.append((char)code);
				}
				EnjinMinecraftPlugin.debug("Raw data received:\n" + input.toString());
				if(commandsrecieved) {
					plugin.saveCommandIDs();
				}
				return tresult; //end of stream reached
			case 0x10:
				EnjinMinecraftPlugin.debug("Packet [0x10](Add Player Group) received.");
				Packet10AddPlayerGroup.handle(bin, plugin);
				break;
			case 0x11:
				EnjinMinecraftPlugin.debug("Packet [0x11](Remove Player Group) received.");
				Packet11RemovePlayerGroup.handle(bin, plugin);
				break;
			case 0x12:
				EnjinMinecraftPlugin.debug("Packet [0x12](Execute Command) received.");
				commandsrecieved = true;
				Packet12ExecuteCommand.handle(bin, plugin);
				break;
			case 0x13:
				EnjinMinecraftPlugin.debug("Packet [0x13](Execute command as Player) received.");
				commandsrecieved = true;
				Packet13ExecuteCommandAsPlayer.handle(bin, plugin);
				break;
			case 0x0A:
				EnjinMinecraftPlugin.debug("Packet [0x0A](New Line) received, ignoring...");
				break;
			case 0x0D:
				EnjinMinecraftPlugin.debug("Packet [0x0D](Carriage Return) received, ignoring...");
				break;
			case 0x14:
				EnjinMinecraftPlugin.debug("Packet [0x14](Newer Version) received.");
				if(EnjinMinecraftPlugin.bukkitversion) {
					Packet14DummyHandler.handle(bin, plugin);
				}else {
					Packet14NewerVersion.handle(bin, plugin);
				}
				break;
			case 0x15:
				EnjinMinecraftPlugin.debug("Packet [0x15](Remote Config Update) received.");
				Packet15RemoteConfigUpdate.handle(bin, plugin);
				break;
			case 0x16:
				EnjinMinecraftPlugin.debug("Packet [0x16](Multi-user Notice) received.");
				Packet16MultiUserNotice.handle(bin, plugin);
				break;
			case 0x17:
				EnjinMinecraftPlugin.debug("Packet [0x17](Add Whitelist Players) received.");
				Packet17AddWhitelistPlayers.handle(bin, plugin);
				break;
			case 0x18:
				EnjinMinecraftPlugin.debug("Packet [0x18](Remove Players From Whitelist) received.");
				Packet18RemovePlayersFromWhitelist.handle(bin, plugin);
				break;
			case 0x19:
				EnjinMinecraftPlugin.debug("Packet [0x19](Enjin Status) received.");
				tresult = PacketUtilities.readString(bin);
				break;
			case 0x1A:
				EnjinMinecraftPlugin.debug("Packet [0x1A](Ban Player) received.");
				Packet1ABanPlayers.handle(bin, plugin);
				break;
			case 0x1B:
				EnjinMinecraftPlugin.debug("Packet [0x1B](Pardon Player) received.");
				Packet1BPardonPlayers.handle(bin, plugin);
				break;
			case 0x1D:
				EnjinMinecraftPlugin.debug("Packet [0x1D](Player Purchase) received.");
				Packet1DPlayerPurchase.handle(bin, plugin);
				break;
			case 0x1E:
				EnjinMinecraftPlugin.debug("Packet [0x1E] (Commands Received) received.");
				Packet1ECommandsReceived.handle(bin, plugin);
				commandsrecieved = true;
				break;
			case 0x3C:
				EnjinMinecraftPlugin.debug("Packet [0x3C](Enjin Maintenance Page) received. Aborting sync.");
				bin.reset();
				StringBuilder input1 = new StringBuilder();
				while((code = bin.read()) != -1) {
					input1.append((char)code);
				}
				EnjinMinecraftPlugin.debug("Raw data received:\n" + input1.toString());
				return "retry_later";
			default :
				EnjinMinecraftPlugin.debug("[Enjin] Received an invalid opcode: " + code);
				bin.reset();
				StringBuilder input2 = new StringBuilder();
				while((code = bin.read()) != -1) {
					input2.append((char)code);
				}
				EnjinMinecraftPlugin.debug("Raw data received:\n" + input2.toString());
				return "invalid_op\nRaw data received:\n" + input2.toString();
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
			if(plugin.supportsUUID()) {
				builder.append(p.getUniqueId().toString() + ":" + p.getName());
			}else {
				builder.append(p.getName());
			}
		}
		if(builder.length() > 2) {
			builder.deleteCharAt(0);
		}
		return builder.toString();
	}
	
	private String getGroups() {
		try {
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
			if(plugin.permissionsnotworking) {
				Player[] players = plugin.getServer().getOnlinePlayers();
				for(Player p : players) {
					if(p.hasPermission("enjin.notify.permissionsnotworking")) {
						p.sendMessage(ChatColor.DARK_GREEN + "[Enjin Minecraft Plugin] Your permissions plugin is properly configured now.");
					}
				}
				plugin.permissionsnotworking = false;
			}
			return builder.toString();
		}catch(Exception e) {
			if(!plugin.permissionsnotworking) {
				Player[] players = plugin.getServer().getOnlinePlayers();
				for(Player p : players) {
					if(p.hasPermission("enjin.notify.permissionsnotworking")) {
						p.sendMessage(ChatColor.DARK_RED + "[Enjin Minecraft Plugin] Your permissions plugin is not configured correctly. Groups and permissions will not update. Check your server.log for more details.");
					}
				}
			}
			plugin.permissionsnotworking = true;
		}
		return "";
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
	
	//Get world times strings
	private String getTimes() {
		StringBuilder builder = new StringBuilder();
		for(World w : Bukkit.getWorlds()) {
			//Make sure it's a normal envrionment, as the end and the
			//nether don't have any weather.
			if(w.getEnvironment() == Environment.NORMAL) {
				if(builder.length() > 0) {
					builder.append(";");
				}
				builder.append(w.getName() + ":" + Long.toString(w.getTime()) + ",");
				int moonphase = (int) ((w.getFullTime()/24000)%8);
				builder.append(Integer.toString(moonphase) + ",");
				if(w.hasStorm()) {
					if(w.isThundering()) {
						builder.append("2");
					}else {
						builder.append("1");
					}
				}else {
					builder.append("0");
				}
			}
		}
		return builder.toString();
	}
    
    private String base64Encode(String encode) {
        if(encode == null) {
            return "";
        }
        return javax.xml.bind.DatatypeConverter.printBase64Binary(encode.getBytes());
    }
}
