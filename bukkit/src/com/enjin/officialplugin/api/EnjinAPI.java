package com.enjin.officialplugin.api;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.Proxy;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import com.enjin.officialplugin.EnjinMinecraftPlugin;
import com.enjin.officialplugin.points.ErrorConnectingToEnjinException;
import com.enjin.officialplugin.points.PlayerDoesNotExistException;
import com.enjin.officialplugin.threaded.UpdateHeadsThread;

public class EnjinAPI {

	/**
	 * This gets a list of all the tags assigned to a player. Please remember to use this in an async thread
	 * otherwise you will lock up your server while the thread waits for an answer.
	 * @param player the player name to get the tags for.
	 * @return the tags assigned to the player. If there are no tags the size is 0.
	 * @throws PlayerDoesNotExistException if the player does not exist on the website.
	 * @throws ErrorConnectingToEnjinException if there is an error connecting to the website.
	 */
	public static ConcurrentHashMap<String, PlayerTag> getPlayerTags(String player) throws PlayerDoesNotExistException, ErrorConnectingToEnjinException {
		ConcurrentHashMap<String, PlayerTag> tags = new ConcurrentHashMap<String, PlayerTag>();
		try {
			EnjinMinecraftPlugin.debug("Connecting to Enjin to retrieve tags for player " + player);
			URL enjinurl = getPlayerTagsUrl();
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

			StringBuilder builder = new StringBuilder();
			builder.append("authkey=" + encode(EnjinMinecraftPlugin.hash));
			builder.append("&player=" + player);
			con.setRequestProperty("Content-Length", String.valueOf(builder.length()));
			EnjinMinecraftPlugin.debug("Sending content: \n" + builder.toString());
			con.getOutputStream().write(builder.toString().getBytes());
			//System.out.println("Getting input stream...");
			InputStream in = con.getInputStream();
			//System.out.println("Handling input stream...");
			String json = UpdateHeadsThread.parseInput(in);

			EnjinMinecraftPlugin.debug("Content of player tags query:\n" + json);
			//Let's parse the json
			JSONParser parser = new JSONParser();

			Object parsedjson = parser.parse(json);
			if(parsedjson instanceof JSONArray) {
				JSONArray array = (JSONArray)parsedjson;
				for(Object obj : array) {
					if(obj instanceof JSONObject) {
						JSONObject object = (JSONObject) obj;
						int id = -1;
						if(object.get("tag_id") != null) {
							String tag_id = object.get("tag_id").toString();
							try {
								id = Integer.parseInt(tag_id);
							}catch(NumberFormatException e) {
								
							}
						}
						String name = "";
						if(object.get("tagname") != null) {
							name = object.get("tagname").toString();
						}
						long expiry = 0;
						Object oexpiry = object.get("expiry_time");
						if(oexpiry != null) {
							//We need to convert from Unix timestamp to Java timestamp.
							expiry = Long.parseLong(oexpiry.toString())*1000;
						}
						PlayerTag tag = new PlayerTag(id, name, expiry);
						Iterator<Map.Entry> es = object.entrySet().iterator();
						while(es.hasNext()) {
							Entry next = es.next();
							String key = next.getKey().toString();
							if(!key.equalsIgnoreCase("tag_id") && !key.equalsIgnoreCase("tagname") && !key.equalsIgnoreCase("expiry_time")) {
								tag.customtags.put(key, next.getValue());
							}
						}
						tags.put(String.valueOf(id), tag);
					}
				}
			}else if(parsedjson instanceof JSONObject) {
				JSONObject array = (JSONObject) parsedjson;
				String error = array.get("error").toString();
				throw new PlayerDoesNotExistException(error);
			}else {
				throw new ErrorConnectingToEnjinException("Unable to parse recieved JSON.");
			}
		} catch (IOException e) {
			throw new ErrorConnectingToEnjinException("Unable to connect to enjin to get flags.");
		} catch (PlayerDoesNotExistException e) {
			throw e;
		} catch (Throwable e) {
			throw new ErrorConnectingToEnjinException("Unable to connect to enjin to get flags.");
		}
		return tags;
	}

	/**
	 * This gets a list of all the tags assigned to a player. Please remember to use this in an async thread
	 * otherwise you will lock up your server while the thread waits for an answer.
	 * @param uuid the player UUID to get the tags for.
	 * @return the tags assigned to the player. If there are no tags the size is 0.
	 * @throws PlayerDoesNotExistException if the player does not exist on the website.
	 * @throws ErrorConnectingToEnjinException if there is an error connecting to the website.
	 */
	public static ConcurrentHashMap<String, PlayerTag> getPlayerTags(UUID uuid) throws PlayerDoesNotExistException, ErrorConnectingToEnjinException {
		ConcurrentHashMap<String, PlayerTag> tags = new ConcurrentHashMap<String, PlayerTag>();
		try {
			EnjinMinecraftPlugin.debug("Connecting to Enjin to retrieve tags for UUID " + uuid.toString());
			URL enjinurl = getPlayerTagsUrl();
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

			StringBuilder builder = new StringBuilder();
			builder.append("authkey=" + encode(EnjinMinecraftPlugin.hash));
			builder.append("&player_uuid=" + encode(uuid.toString()));
			con.setRequestProperty("Content-Length", String.valueOf(builder.length()));
			EnjinMinecraftPlugin.debug("Sending content: \n" + builder.toString());
			con.getOutputStream().write(builder.toString().getBytes());
			//System.out.println("Getting input stream...");
			InputStream in = con.getInputStream();
			//System.out.println("Handling input stream...");
			String json = UpdateHeadsThread.parseInput(in);

			EnjinMinecraftPlugin.debug("Content of player tags query:\n" + json);
			//Let's parse the json
			JSONParser parser = new JSONParser();

			Object parsedjson = parser.parse(json);
			if(parsedjson instanceof JSONArray) {
				JSONArray array = (JSONArray)parsedjson;
				for(Object obj : array) {
					if(obj instanceof JSONObject) {
						JSONObject object = (JSONObject) obj;
						int id = -1;
						if(object.get("tag_id") != null) {
							String tag_id = object.get("tag_id").toString();
							try {
								id = Integer.parseInt(tag_id);
							}catch(NumberFormatException e) {
								
							}
						}
						String name = "";
						if(object.get("tagname") != null) {
							name = object.get("tagname").toString();
						}
						long expiry = 0;
						Object oexpiry = object.get("expiry_time");
						if(oexpiry != null) {
							//We need to convert from Unix timestamp to Java timestamp.
							expiry = Long.parseLong(oexpiry.toString())*1000;
						}
						PlayerTag tag = new PlayerTag(id, name, expiry);
						Iterator<Map.Entry> es = object.entrySet().iterator();
						while(es.hasNext()) {
							Entry next = es.next();
							String key = next.getKey().toString();
							if(!key.equalsIgnoreCase("tag_id") && !key.equalsIgnoreCase("tagname") && !key.equalsIgnoreCase("expiry_time")) {
								tag.customtags.put(key, next.getValue());
							}
						}
						tags.put(String.valueOf(id), tag);
					}
				}
			}else if(parsedjson instanceof JSONObject) {
				JSONObject array = (JSONObject) parsedjson;
				String error = array.get("error").toString();
				throw new PlayerDoesNotExistException(error);
			}else {
				throw new ErrorConnectingToEnjinException("Unable to parse recieved JSON.");
			}
		} catch (IOException e) {
			throw new ErrorConnectingToEnjinException("Unable to connect to enjin to get tags.");
		} catch (PlayerDoesNotExistException e) {
			throw e;
		} catch (Throwable e) {
			throw new ErrorConnectingToEnjinException("Unable to connect to enjin to get tags.");
		}
		return tags;
	}

	private static URL getPlayerTagsUrl() throws Throwable {
		return new URL((EnjinMinecraftPlugin.usingSSL ? "https" : "http") + EnjinMinecraftPlugin.apiurl + "minecraft-get-tags");
	}

	private static String encode(String in) throws UnsupportedEncodingException {
		return URLEncoder.encode(in, "UTF-8");
		//return in;
	}
}
