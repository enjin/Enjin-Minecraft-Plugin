package com.enjin.officialplugin.shop;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.Proxy;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLEncoder;

import net.minecraft.entity.player.EntityPlayerMP;

import com.enjin.officialplugin.ChatColor;
import com.enjin.officialplugin.EnjinMinecraftPlugin;

public class PlayerShopGetter implements Runnable {
	
	EntityPlayerMP player;
	ShopListener listener;
	
	public PlayerShopGetter(ShopListener listener, EntityPlayerMP player) {
		this.listener = listener;
		this.player = player;
	}

	@Override
	public void run() {
		StringBuilder builder = new StringBuilder();
		try {
			EnjinMinecraftPlugin.debug("Connecting to Enjin for shop data for player...");
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
			builder.append("&player=" + encode(player.username)); //current player
			con.setRequestProperty("Content-Length", String.valueOf(builder.length()));
			EnjinMinecraftPlugin.debug("Sending content: \n" + builder.toString());
			con.getOutputStream().write(builder.toString().getBytes());
			//System.out.println("Getting input stream...");
			InputStream in = con.getInputStream();
			//System.out.println("Handling input stream...");
			String json = parseInput(in);
			PlayerShopsInstance shops = ShopUtils.parseShopsJSON(json);
			listener.activeshops.put(player.username.toLowerCase(), shops);
			//listener.playersdisabledchat.put(player.username.toLowerCase(), player.username);
			listener.sendPlayerInitialShopData(player, shops);
			return;
		} catch (SocketTimeoutException e) {
			e.printStackTrace();
		} catch (Throwable t) {
			t.printStackTrace();
		}
		player.sendChatToPlayer(ChatColor.RED + "There was a problem loading the shop, please try again later.");
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

	private URL getUrl() throws Throwable {
		return new URL((EnjinMinecraftPlugin.usingSSL ? "https" : "http") + EnjinMinecraftPlugin.apiurl + "minecraft-shop");
	}

	private String encode(String in) throws UnsupportedEncodingException {
		return URLEncoder.encode(in, "UTF-8");
		//return in;
	}

}
