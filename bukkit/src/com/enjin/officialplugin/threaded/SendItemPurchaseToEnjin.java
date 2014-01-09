package com.enjin.officialplugin.threaded;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.Proxy;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLEncoder;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.enjin.officialplugin.EnjinErrorReport;
import com.enjin.officialplugin.EnjinMinecraftPlugin;
import com.enjin.officialplugin.shop.ShopItemBuyer;
import com.enjin.officialplugin.shop.ShopUtils;

public class SendItemPurchaseToEnjin implements Runnable {
	
	ShopItemBuyer buyer;
	Player player;
	EnjinMinecraftPlugin plugin;
	
	public SendItemPurchaseToEnjin(EnjinMinecraftPlugin plugin, ShopItemBuyer buyer, Player player) {
		this.buyer = buyer;
		this.player = player;
		this.plugin = plugin;
	}

	@Override
	public void run() {
		boolean successful = false;
		StringBuilder builder = new StringBuilder();
		try {
			EnjinMinecraftPlugin.debug("Connecting to Enjin to send item purchase...");
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
			builder.append("&item_id=" + encode(buyer.getItem().getId()));
			builder.append("&player=" + encode(player.getName()));
			builder.append("&custom_points=0&custom_price=0");
			for(String option : buyer.getOptions()) {
				builder.append("&" + option);
			}
			con.setRequestProperty("Content-Length", String.valueOf(builder.length()));
			EnjinMinecraftPlugin.debug("Sending content: \n" + builder.toString());
			con.getOutputStream().write(builder.toString().getBytes());
			//System.out.println("Getting input stream...");
			InputStream in = con.getInputStream();
			//Let's get all the input, then parse it.
			String json = parseInput(in);
			EnjinMinecraftPlugin.debug("Returned content for purchase:\n" + json);
			//Let's parse the json
			JSONParser parser = new JSONParser();
			try {
				JSONObject array = (JSONObject) parser.parse(json);
				if(array.containsKey("success")) {
					boolean success = ShopUtils.getBoolean(array.get("success"));
					String chatcolor = ChatColor.DARK_PURPLE.toString();
					if(!success) {
						chatcolor = ChatColor.RED.toString();
					}
					String message = array.get("message").toString();
					player.sendMessage(chatcolor + message);
				}else {
					//An error has occurred
					String message = array.get("error").toString();
					player.sendMessage(ChatColor.RED + message);
				}
			}catch (ParseException e) {
				player.sendMessage(ChatColor.DARK_RED + "There was an error parsing the returned data for your purchase. Please try again later.");
				e.printStackTrace();
			}
		} catch (SocketTimeoutException e) {
			plugin.lasterror = new EnjinErrorReport(e, "In game purchase. Information sent:\n" + builder.toString());
		} catch (Throwable t) {
			if(EnjinMinecraftPlugin.debug) {
				t.printStackTrace();
			}
			plugin.lasterror = new EnjinErrorReport(t, "In game purchase. Information sent:\n" + builder.toString());
			EnjinMinecraftPlugin.enjinlogger.warning(plugin.lasterror.toString());
		}
	}

	private URL getUrl() throws Throwable {
		return new URL((EnjinMinecraftPlugin.usingSSL ? "https" : "http") + EnjinMinecraftPlugin.apiurl + "minecraft-ingame-purchase");
	}

	private String encode(String in) throws UnsupportedEncodingException {
		return URLEncoder.encode(in, "UTF-8");
		//return in;
	}

	public static String parseInput(InputStream in) throws IOException {
		byte[] buffer = new byte[1024];
		int bytesRead = in.read(buffer);
		StringBuilder builder = new StringBuilder();
		while(bytesRead > 0) {
			builder.append(new String(buffer, 0, bytesRead, "UTF-8"));
			bytesRead = in.read(buffer);
		}
		return builder.toString();
	}

}
