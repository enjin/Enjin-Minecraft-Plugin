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

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.WorldServer;
import net.minecraft.world.biome.WorldChunkManager;
import net.minecraft.world.storage.WorldInfo;
import net.minecraftforge.common.DimensionManager;

import com.enjin.officialplugin.ChatColor;
import com.enjin.officialplugin.EnjinErrorReport;
import com.enjin.officialplugin.EnjinMinecraftPlugin;
import com.enjin.officialplugin.packets.Packet10AddPlayerGroup;
import com.enjin.officialplugin.packets.Packet11RemovePlayerGroup;
import com.enjin.officialplugin.packets.Packet12ExecuteCommand;
import com.enjin.officialplugin.packets.Packet13ExecuteCommandAsPlayer;
import com.enjin.officialplugin.packets.Packet14NewerVersion;
import com.enjin.officialplugin.packets.Packet15RemoteConfigUpdate;
import com.enjin.officialplugin.packets.Packet16MultiUserNotice;
import com.enjin.officialplugin.packets.Packet17AddWhitelistPlayers;
import com.enjin.officialplugin.packets.Packet18RemovePlayersFromWhitelist;
import com.enjin.officialplugin.packets.Packet1ABanPlayers;
import com.enjin.officialplugin.packets.Packet1BPardonPlayers;
import com.enjin.officialplugin.packets.Packet1DPlayerPurchase;
import com.enjin.officialplugin.packets.PacketUtilities;

import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.ModContainer;

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
				MinecraftServer.getServer().logWarning("[Enjin Minecraft Plugin] SSL test connection failed, The plugin will use http without SSL. This may be less secure.");
				EnjinMinecraftPlugin.enjinlogger.warning("SSL test connection failed, The plugin will use http without SSL. This may be less secure.");
			}
		}
		boolean successful = false;
		StringBuilder builder = new StringBuilder();
		try {
			plugin.debug("Connecting to Enjin...");
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
				builder.append("&maxplayers=" + encode(String.valueOf(MinecraftServer.getServer().getConfigurationManager().getMaxPlayers()))); //max players
				builder.append("&mc_version=" + encode(plugin.mcversion));
			}
			builder.append("&players=" + encode(String.valueOf(MinecraftServer.getServer().getConfigurationManager().getCurrentPlayerCount()))); //current players
			builder.append("&hasranks=" + encode("FALSE"));
			builder.append("&pluginversion=" + encode(plugin.getVersion()));
			//We only want to send the list of plugins every hour
			if(plugindelay++ >= 59) {
				builder.append("&mods=" + encode(getMods()));
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
			con.setRequestProperty("Content-Length", String.valueOf(builder.length()));
			plugin.debug("Sending content: \n" + builder.toString());
			con.getOutputStream().write(builder.toString().getBytes());
			//System.out.println("Getting input stream...");
			InputStream in = con.getInputStream();
			//System.out.println("Handling input stream...");
			String success = handleInput(in);
			if(success.equalsIgnoreCase("ok")) {
				successful = true;
				if(plugin.unabletocontactenjin) {
					plugin.unabletocontactenjin = false;
					String[] players = MinecraftServer.getServer().getConfigurationManager().getAllUsernames();
					for(String player : players) {
						if(MinecraftServer.getServer().getConfigurationManager().getOps().contains(player.toLowerCase())) {
							EntityPlayerMP rplayer = MinecraftServer.getServer().getConfigurationManager().getPlayerForUsername(player);
							rplayer.addChatMessage(ChatColor.DARK_GREEN + "[Enjin Minecraft Plugin] Connection to Enjin re-established!");
							MinecraftServer.getServer().logInfo("[Enjin Minecraft Plugin] Connection to Enjin re-established!");
						}
					}
				}
			}else if(success.equalsIgnoreCase("auth_error")) {
				plugin.authkeyinvalid = true;
				EnjinMinecraftPlugin.enjinlogger.warning("[Enjin Minecraft Plugin] Auth key invalid. Please regenerate on the enjin control panel.");
				MinecraftServer.getServer().logWarning("[Enjin Minecraft Plugin] Auth key invalid. Please regenerate on the enjin control panel.");
				plugin.stopTask();
				String[] players = MinecraftServer.getServer().getConfigurationManager().getAllUsernames();
				for(String player : players) {
					if(MinecraftServer.getServer().getConfigurationManager().getOps().contains(player.toLowerCase())) {
						EntityPlayerMP rplayer = MinecraftServer.getServer().getConfigurationManager().getPlayerForUsername(player);
						rplayer.addChatMessage(ChatColor.DARK_RED + "[Enjin Minecraft Plugin] Auth key is invalid. Please generate a new one.");
					}
				}
				successful = false;
			}else if(success.equalsIgnoreCase("bad_data")) {
				EnjinMinecraftPlugin.enjinlogger.warning("[Enjin Minecraft Plugin] Oops, we sent bad data, please send the enjin.log file to enjin to debug.");
				plugin.lasterror = new EnjinErrorReport("Enjin reported bad data", "Regular synch. Information sent:\n" + builder.toString());
				//MinecraftServer.getServer().logWarning("[Enjin Minecraft Plugin] Oops, we sent bad data, please send the enjin.log file to enjin to debug.");
				successful = false;
			}else if(success.equalsIgnoreCase("retry_later")) {
				EnjinMinecraftPlugin.enjinlogger.info("[Enjin Minecraft Plugin] Enjin said to wait, saving data for next sync.");
				//MinecraftServer.getServer().logInfo("[Enjin Minecraft Plugin] Enjin said to wait, saving data for next sync.");
				successful = false;
			}else if(success.equalsIgnoreCase("connect_error")) {
				EnjinMinecraftPlugin.enjinlogger.info("[Enjin Minecraft Plugin] Enjin is having something going on, if you continue to see this error please report it to enjin.");
				plugin.lasterror = new EnjinErrorReport("Enjin said there's a connection error somewhere.", "Regular synch. Information sent:\n" + builder.toString());
				//MinecraftServer.getServer().logInfo("[Enjin Minecraft Plugin] Enjin is having something going on, if you continue to see this error please report it to enjin.");
				successful = false;
			}else if(success.startsWith("invalid_op")) {
				plugin.lasterror = new EnjinErrorReport(success, "Regular synch. Information sent:\n" + builder.toString());
				successful = false;
			}else {
				EnjinMinecraftPlugin.enjinlogger.info("[Enjin Minecraft Plugin] Something happened on sync, if you continue to see this error please report it to enjin.");
				EnjinMinecraftPlugin.enjinlogger.info("Response code: " + success);
				MinecraftServer.getServer().logInfo("[Enjin Minecraft Plugin] Something happened on sync, if you continue to see this error please report it to enjin.");
				MinecraftServer.getServer().logInfo("[Enjin Minecraft Plugin] Response code: " + success);
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
				MinecraftServer.getServer().logWarning("[Enjin Minecraft Plugin] Timeout, the enjin server didn't respond within the required time. Please be patient and report this bug to enjin.");
				numoffailedtries = 0;
				plugin.noEnjinConnectionEvent();
			}
			plugin.lasterror = new EnjinErrorReport(e, "Regular synch. Information sent:\n" + builder.toString());
		} catch (Throwable t) {
			//We don't need to spam the console every minute if the synch didn't complete correctly.
			if(numoffailedtries++ > 5) {
				EnjinMinecraftPlugin.enjinlogger.warning("[Enjin Minecraft Plugin] Oops, we didn't get a proper response, we may be doing some maintenance. Please be patient and report this bug to enjin if it persists.");
				MinecraftServer.getServer().logWarning("[Enjin Minecraft Plugin] Oops, we didn't get a proper response, we may be doing some maintenance. Please be patient and report this bug to enjin if it persists.");
				numoffailedtries = 0;
				plugin.noEnjinConnectionEvent();
			}
			if(plugin.debug) {
				t.printStackTrace();
			}
			plugin.lasterror = new EnjinErrorReport(t, "Regular synch. Information sent:\n" + builder.toString());
			EnjinMinecraftPlugin.enjinlogger.warning(plugin.lasterror.toString());
		}
		if(!successful) {
			plugin.debug("Synch unsuccessful.");
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
			plugin.debug("Synch successful.");
			if(plugindelay >= 59) {
				plugindelay = 0;
			}
		}
		if(plugin.collectstats) {
			//new WriteStats(plugin).write("stats.stats");
			//plugin.debug("Stats saved to stats.stats.");
		}
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
	
	/*
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
	}*/
	
	/*
	private String getPlayerGroups() {
		removedplayerperms.clear();
		HashMap<String, String> theperms = new HashMap<String, String>();
		Iterator<Entry<String, String>> es = plugin.playerperms.entrySet().iterator();
		
		//With the push command, we need to limit how many ranks we send
		//with each synch.
		for(int k = 0; es.hasNext() && k < 3000; k++) {
			Entry<String, String> entry = es.next();
			StringBuilder perms = new StringBuilder();
			//Let's get global groups
			LinkedList<String> globalperms = new LinkedList<String>();
			//We don't want to get global groups with plugins that don't support it.
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
	}*/
	
	private String encode(String in) throws UnsupportedEncodingException {
		return URLEncoder.encode(in, "UTF-8");
		//return in;
	}
	
	public String handleInput(InputStream in) throws IOException {
		String tresult = "Unknown Error";
		BufferedInputStream bin = new BufferedInputStream(in);
		bin.mark(Integer.MAX_VALUE);
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
			case 0x15:
				plugin.debug("Packet [0x15](Remote Config Update) received.");
				Packet15RemoteConfigUpdate.handle(bin, plugin);
				break;
			case 0x16:
				plugin.debug("Packet [0x16](Multi-user Notice) received.");
				Packet16MultiUserNotice.handle(bin, plugin);
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
				plugin.debug("Packet [0x19](Enjin Status) received.");
				tresult = PacketUtilities.readString(bin);
				break;
			case 0x1A:
				plugin.debug("Packet [0x1A](Ban Player) received.");
				Packet1ABanPlayers.handle(bin, plugin);
				break;
			case 0x1B:
				plugin.debug("Packet [0x1B](Pardon Player) received.");
				Packet1BPardonPlayers.handle(bin, plugin);
				break;
			case 0x1D:
				EnjinMinecraftPlugin.debug("Packet [0x1D](Player Purchase) received.");
				Packet1DPlayerPurchase.handle(bin, plugin);
				break;
			case 0x3C:
				plugin.debug("Packet [0x3C](Enjin Maintenance Page) received. Aborting sync.");
				bin.reset();
				StringBuilder input1 = new StringBuilder();
				while((code = bin.read()) != -1) {
					input1.append((char)code);
				}
				plugin.debug("Raw data received:\n" + input1.toString());
				return "retry_later";
			default :
				plugin.debug("[Enjin] Received an invalid opcode: " + code);
				bin.reset();
				StringBuilder input2 = new StringBuilder();
				while((code = bin.read()) != -1) {
					input2.append((char)code);
				}
				plugin.debug("Raw data received:\n" + input2.toString());
				return "invalid_op\nRaw data received:\n" + input2.toString();
			}
		}
	}

	private String getMods() {
		StringBuilder builder = new StringBuilder();
		List<ModContainer> modlist = Loader.instance().getActiveModList();
		for(ModContainer p : modlist) {
			builder.append("," + p.getName());
		}
		if(builder.length() > 2) {
			builder.deleteCharAt(0);
		}
		return builder.toString();
	}
	
	private String getPlayers() {
		StringBuilder builder = new StringBuilder();
		for(String p : MinecraftServer.getServer().getConfigurationManager().getAllUsernames()) {
			builder.append(',');
			builder.append(p);
		}
		if(builder.length() > 2) {
			builder.deleteCharAt(0);
		}
		return builder.toString();
	}
	
	/*
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
	}*/
	
	private String getWorlds() {
		StringBuilder builder = new StringBuilder();
		WorldServer[] worldservers = MinecraftServer.getServer().worldServers;
		LinkedList<String> worlds = new LinkedList<String>();
		for(WorldServer world : worldservers) {
			if(!worlds.contains(world.getWorldInfo().getWorldName())) {
				builder.append(',');
				builder.append(world.getWorldInfo().getWorldName());
				worlds.add(world.getWorldInfo().getWorldName());
			}
		}
		if(builder.length() > 2) {
			builder.deleteCharAt(0);
		}
		return builder.toString();
	}
	
	//Get world times strings
	private String getTimes() {
		StringBuilder builder = new StringBuilder();
		LinkedList<String> worlds = new LinkedList<String>();
		for(WorldServer w : MinecraftServer.getServer().worldServers) {
			//Make sure it's a normal envrionment, as the end and the
			//nether don't have any weather.
			WorldInfo worldinfo = w.getWorldInfo();
			if(w.getWorldInfo().getDimension() == 0 && !worlds.contains(worldinfo.getWorldName())) {
				if(builder.length() > 0) {
					builder.append(";");
				}
				worlds.add(worldinfo.getWorldName());
				builder.append(w.getWorldInfo().getWorldName() + ":" + Long.toString(w.getWorldInfo().getWorldTime()) + ",");
				int moonphase = (int) ((worldinfo.getWorldTotalTime()/24000)%8);
				builder.append(Integer.toString(moonphase) + ",");
				if(worldinfo.isRaining()) {
					if(worldinfo.isThundering()) {
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
}
