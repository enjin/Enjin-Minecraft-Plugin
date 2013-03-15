package com.enjin.officialplugin.threaded;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.Proxy;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Map.Entry;
import java.util.Set;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;

import com.enjin.officialplugin.ChatColor;
import com.enjin.officialplugin.ConfigValueTypes;
import com.enjin.officialplugin.EnjinErrorReport;
import com.enjin.officialplugin.EnjinMinecraftPlugin;
import com.enjin.officialplugin.packets.PacketUtilities;

public class ConfigSender implements Runnable {
	
	EnjinMinecraftPlugin plugin;
	
	public ConfigSender(EnjinMinecraftPlugin plugin) {
		this.plugin = plugin;
	}

	@Override
	public void run() {
		//Only run the ssl test on first run.
				if(EnjinMinecraftPlugin.usingSSL) {
					if(!plugin.testHTTPSconnection()) {
						EnjinMinecraftPlugin.usingSSL = false;
						MinecraftServer.getServer().logWarning("SSL test connection failed, The plugin will use http without SSL. This may be less secure.");
						EnjinMinecraftPlugin.enjinlogger.warning("SSL test connection failed, The plugin will use http without SSL. This may be less secure.");
					}
				}
				StringBuilder builder = new StringBuilder();
				try {
					plugin.debug("Connecting to Enjin to send config...");
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
					Set<Entry<String, ConfigValueTypes>> es = plugin.configvalues.entrySet();
					for(Entry<String, ConfigValueTypes> entry : es) {
						switch(entry.getValue()) {
						case FORBIDDEN:
							//Don't transmit the value if we can't change it.
							break;
						case BOOLEAN:
							boolean istrue = plugin.config.getBoolean(entry.getKey());
							builder.append("&" + entry.getKey().replaceAll("[.]", "_") + "=" + (istrue ? "1" : "0"));
							break;
							//we only need to do something special for booleans, all the rest can be read in as plain text.
						default:
							builder.append("&" + entry.getKey().replaceAll("[.]", "_") + "=" + plugin.config.getString(entry.getKey()));
						}
					}
					con.setRequestProperty("Content-Length", String.valueOf(builder.length()));
					plugin.debug("Sending content: \n" + builder.toString());
					con.getOutputStream().write(builder.toString().getBytes());
					//System.out.println("Getting input stream...");
					InputStream in = con.getInputStream();
					String success = handleInput(in);
					if(success.equalsIgnoreCase("ok")) {
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
					}else if(success.equalsIgnoreCase("bad_data")) {
						EnjinMinecraftPlugin.enjinlogger.warning("[Enjin Minecraft Plugin] Oops, we sent bad data, please send the enjin.log file to enjin to debug.");
						MinecraftServer.getServer().logWarning("[Enjin Minecraft Plugin] Oops, we sent bad data, please send the enjin.log file to enjin to debug.");
					}else if(success.equalsIgnoreCase("retry_later")) {
						EnjinMinecraftPlugin.enjinlogger.info("[Enjin Minecraft Plugin] Enjin said to wait, will retry at next boot.");
						MinecraftServer.getServer().logInfo("[Enjin Minecraft Plugin] Enjin said to wait, will retry at next boot.");
					}else if(success.equalsIgnoreCase("connect_error")) {
						EnjinMinecraftPlugin.enjinlogger.info("[Enjin Minecraft Plugin] Enjin is having something going on, if you continue to see this error please report it to enjin.");
						MinecraftServer.getServer().logInfo("[Enjin Minecraft Plugin] Enjin is having something going on, if you continue to see this error please report it to enjin.");
					}else {
						EnjinMinecraftPlugin.enjinlogger.info("[Enjin Minecraft Plugin] Something happened on sync, if you continue to see this error please report it to enjin.");
						EnjinMinecraftPlugin.enjinlogger.info("Response code: " + success);
						MinecraftServer.getServer().logInfo("[Enjin Minecraft Plugin] Something happened on sync, if you continue to see this error please report it to enjin.");
						MinecraftServer.getServer().logInfo("[Enjin Minecraft Plugin] Response code: " + success);
					}
				} catch (SocketTimeoutException e) {
					//We don't need to spam the console every minute if the synch didn't complete correctly.
					plugin.lasterror = new EnjinErrorReport(e, "Config sender. Information sent:\n" + builder.toString());
				} catch (Throwable t) {
					plugin.lasterror = new EnjinErrorReport(t, "config sender. Information sent:\n" + builder.toString());
					EnjinMinecraftPlugin.enjinlogger.warning(plugin.lasterror.toString());
				}
	}

	private String handleInput(InputStream in) {
		BufferedInputStream bin = new BufferedInputStream(in);
		try {
			return PacketUtilities.readString(bin);
		} catch (IOException e) {
			e.printStackTrace();
			return "string_read_error";
		}
	}

	private URL getUrl() throws Throwable {
		return new URL((EnjinMinecraftPlugin.usingSSL ? "https" : "http") + EnjinMinecraftPlugin.apiurl + "minecraft-config");
	}

	private String encode(String in) throws UnsupportedEncodingException {
		return URLEncoder.encode(in, "UTF-8");
		//return in;
	}

}
