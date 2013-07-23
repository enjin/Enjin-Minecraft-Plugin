package com.enjin.officialplugin.threaded;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.Proxy;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.enjin.officialplugin.EnjinMinecraftPlugin;
import com.enjin.officialplugin.events.HeadsUpdatedEvent;
import com.enjin.officialplugin.heads.HeadData;
import com.enjin.officialplugin.heads.HeadLocation;
import com.enjin.officialplugin.shop.ShopItem;
import com.enjin.officialplugin.shop.ShopItemOptions;
import com.enjin.officialplugin.shop.ShopUtils;

public class UpdateHeadsThread implements Runnable {

	EnjinMinecraftPlugin plugin;
	SimpleDateFormat date = new SimpleDateFormat("dd MMM yyyy");
	SimpleDateFormat time = new SimpleDateFormat("h:mm:ss a z");
	CommandSender sender = null;


	public UpdateHeadsThread(EnjinMinecraftPlugin plugin) {
		super();
		this.plugin = plugin;
	}
	
	public UpdateHeadsThread(EnjinMinecraftPlugin plugin, CommandSender sender) {
		this.plugin = plugin;
		this.sender = sender;
	}
	
	public synchronized void updateHeads() {
		//Let's update all the packages before updating the heads...
		try {
			EnjinMinecraftPlugin.debug("Connecting to Enjin for package data for heads...");
			URL enjinurl = getItemsUrl();
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
			builder.append("authkey=" + encode(EnjinMinecraftPlugin.hash) + "&player=0");
			con.setRequestProperty("Content-Length", String.valueOf(builder.length()));
			EnjinMinecraftPlugin.debug("Sending content: \n" + builder.toString());
			con.getOutputStream().write(builder.toString().getBytes());
			//System.out.println("Getting input stream...");
			InputStream in = con.getInputStream();
			//System.out.println("Handling input stream...");
			String json = parseInput(in);

			EnjinMinecraftPlugin.debug("Content of package data update:\n" + json);
			//Let's parse the json
			JSONParser parser = new JSONParser();
			try {
				JSONArray array = (JSONArray) parser.parse(json);
				plugin.cachedItems.clearShopItems();
				for (Object oitem : array) {
					JSONObject item = (JSONObject) oitem;
					ShopItem sitem = new ShopItem((String) item.get("name"),
							(String) item.get("id"), ShopUtils.getPriceString(item.get("price")),
							(String) item.get("info"));
					Object options = item.get("variables");
					if (options != null && options instanceof JSONArray
							&& ((JSONArray) options).size() > 0) {
						JSONArray joptions = (JSONArray) options;
						@SuppressWarnings("rawtypes")
						Iterator optionsiterator = joptions.iterator();
						while (optionsiterator.hasNext()) {
							JSONObject option = (JSONObject) optionsiterator.next();
							ShopItemOptions soptions = new ShopItemOptions(
									(String) option.get("name"),
									ShopUtils.getPriceString(option.get("pricemin")),
									ShopUtils.getPriceString(option.get("pricemax")));
							sitem.addOption(soptions);
						}
					}
					plugin.cachedItems.addShopItem(sitem);
				}
			} catch (ParseException e) {
				if(sender != null) {
					sender.sendMessage(ChatColor.DARK_RED + "There was an error parsing the shop data, donations won't show package information.");
				}
				e.printStackTrace();
			}
		} catch (SocketTimeoutException e) {
			if(sender != null) {
				sender.sendMessage(ChatColor.DARK_RED + "There was an error connecting to enjin, please try again later.");
			}
		} catch (Throwable t) {
			if(sender != null) {
				sender.sendMessage(ChatColor.DARK_RED + "There was an error syncing the shop's packages, please fill out a support ticket at http://enjin.com/support and include the results of your /enjin report");
			}
			t.printStackTrace();
		}
		//Let's retrieve all the heads!
		try {
			EnjinMinecraftPlugin.debug("Connecting to Enjin for stats data for heads...");
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

			StringBuilder builder = new StringBuilder();
			builder.append("authkey=" + encode(EnjinMinecraftPlugin.hash));
			ArrayList<HeadLocation> itemdonators = plugin.headlocation.getHeads(HeadLocation.Type.RecentItemDonator);
			ArrayList<String> specificitems = new ArrayList<String>();
			for(HeadLocation loc : itemdonators) {
				if(!specificitems.contains(loc.getItemid())) {
					specificitems.add(loc.getItemid());
				}
			}
			if(specificitems.size() > 0) {
				builder.append("&items=");
				for(int i = 0; i < specificitems.size(); i++) {
					if(i > 0) {
						builder.append(",");
					}
					builder.append(specificitems.get(i));
				}
			}
			con.setRequestProperty("Content-Length", String.valueOf(builder.length()));
			EnjinMinecraftPlugin.debug("Sending content: \n" + builder.toString());
			con.getOutputStream().write(builder.toString().getBytes());
			//System.out.println("Getting input stream...");
			InputStream in = con.getInputStream();
			//System.out.println("Handling input stream...");
			String json = parseInput(in);

			EnjinMinecraftPlugin.debug("Content of heads update:\n" + json);
			//Let's parse the json
			JSONParser parser = new JSONParser();
			try {
				JSONObject array = (JSONObject) parser.parse(json);
				if(array.get("recent_purchases") instanceof JSONArray) {
					JSONArray recentpurchases = (JSONArray)array.get("recent_purchases");
					if(recentpurchases != null) {
						plugin.headdata.clearHeadData(HeadLocation.Type.RecentDonator);
						int i = 0;
						for (Object purchase : recentpurchases) {
							if (purchase instanceof JSONObject) {
								JSONObject tpurchase = (JSONObject) purchase;
								String playername = (String) tpurchase.get("player_name");
								String price = tpurchase.get("price").toString();
								String itemid = "";
								Object items = tpurchase.get("items");
								if (items != null && items instanceof JSONArray
										&& ((JSONArray) items).size() > 0) {
									if(((JSONArray) items).size() == 1) {
										itemid = (String) ((JSONArray)items).get(0);
									}else {
										itemid = "Multiple Items";
									}
								}
								String[] signdata = plugin.cachedItems.getSignData(playername, itemid, HeadLocation.Type.RecentDonator, i, price);
								HeadData hd = new HeadData(playername, signdata, HeadLocation.Type.RecentDonator, i);
								plugin.headdata.setHead(hd, false);
								i++;
							}
						}
						Bukkit.getServer().getPluginManager().callEvent(new HeadsUpdatedEvent(HeadLocation.Type.RecentDonator));
					}
				}
				if(array.get("item_purchases") instanceof JSONObject) {
					JSONObject recentitempurchases = (JSONObject)array.get("item_purchases");
					if(recentitempurchases != null) {
						@SuppressWarnings("rawtypes")
						Set items = recentitempurchases.keySet();
						//plugin.headdata.clearHeadData(HeadLocation.Type.RecentItemDonator);
						for (Object item : items) {
							if(item instanceof String) {
								String itemid = (String) item;
								JSONArray itemlist = (JSONArray) recentitempurchases.get(item);
								int i = 0;
								for(Object purchase : itemlist) {
									if (purchase instanceof JSONObject) {
										JSONObject tpurchase = (JSONObject) purchase;
										String playername = (String) tpurchase.get("player_name");
										String price = tpurchase.get("price").toString();
										String[] signdata = plugin.cachedItems.getSignData(playername, itemid, HeadLocation.Type.RecentItemDonator, i, price);
										HeadData hd = new HeadData(playername, signdata, HeadLocation.Type.RecentItemDonator, i, itemid);
										plugin.headdata.setHead(hd, false);
										i++;
									}
								}
								Bukkit.getServer().getPluginManager().callEvent(new HeadsUpdatedEvent(HeadLocation.Type.RecentItemDonator, itemid));
							}
						}
					}
				}
				if(array.get("top_voters_day") instanceof JSONArray) {
					JSONArray topvotersday = (JSONArray)array.get("top_voters_day");
					if(topvotersday != null) {
						plugin.headdata.clearHeadData(HeadLocation.Type.TopDailyVoter);
						int i = 0;
						for (Object voter : topvotersday) {
							if (voter instanceof JSONObject) {
								JSONObject tvoter = (JSONObject) voter;
								String playername = (String) tvoter.get("player_name");
								String votes = tvoter.get("cnt").toString();
								String[] signdata = plugin.cachedItems.getSignData(playername, "", HeadLocation.Type.TopDailyVoter, i, votes);
								HeadData hd = new HeadData(playername, signdata, HeadLocation.Type.TopDailyVoter, i);
								plugin.headdata.setHead(hd, false);
								i++;
							}
						}
						Bukkit.getServer().getPluginManager().callEvent(new HeadsUpdatedEvent(HeadLocation.Type.TopDailyVoter));
					}
				}
				if(array.get("top_voters_week") instanceof JSONArray) {
					JSONArray topvotersweek = (JSONArray)array.get("top_voters_week");
					if(topvotersweek != null) {
						plugin.headdata.clearHeadData(HeadLocation.Type.TopWeeklyVoter);
						int i = 0;
						for (Object voter : topvotersweek) {
							if (voter instanceof JSONObject) {
								JSONObject tvoter = (JSONObject) voter;
								String playername = (String) tvoter.get("player_name");
								String votes = tvoter.get("cnt").toString();
								String[] signdata = plugin.cachedItems.getSignData(playername, "", HeadLocation.Type.TopWeeklyVoter, i, votes);
								HeadData hd = new HeadData(playername, signdata, HeadLocation.Type.TopWeeklyVoter, i);
								plugin.headdata.setHead(hd, false);
								i++;
							}
						}
						Bukkit.getServer().getPluginManager().callEvent(new HeadsUpdatedEvent(HeadLocation.Type.TopWeeklyVoter));
					}
				}
				if(array.get("top_voters_month") instanceof JSONArray) {
					JSONArray topvotersmonth = (JSONArray)array.get("top_voters_month");
					if(topvotersmonth != null) {
						plugin.headdata.clearHeadData(HeadLocation.Type.TopMonthlyVoter);
						int i = 0;
						for (Object voter : topvotersmonth) {
							if (voter instanceof JSONObject) {
								JSONObject tvoter = (JSONObject) voter;
								String playername = (String) tvoter.get("player_name");
								String votes = tvoter.get("cnt").toString();
								String[] signdata = plugin.cachedItems.getSignData(playername, "", HeadLocation.Type.TopMonthlyVoter, i, votes);
								HeadData hd = new HeadData(playername, signdata, HeadLocation.Type.TopMonthlyVoter, i);
								plugin.headdata.setHead(hd, false);
								i++;
							}
						}
						Bukkit.getServer().getPluginManager().callEvent(new HeadsUpdatedEvent(HeadLocation.Type.TopMonthlyVoter));
					}
				}
				if(array.get("recent_voters") instanceof JSONArray) {
					JSONArray recentvoters = (JSONArray)array.get("recent_voters");
					if(recentvoters != null) {
						plugin.headdata.clearHeadData(HeadLocation.Type.RecentVoter);
						int i = 0;
						for (Object voter : recentvoters) {
							if (voter instanceof JSONObject) {
								JSONObject tvoter = (JSONObject) voter;
								String playername = (String) tvoter.get("player_name");
								//We need to convert from Unix time stamp to a date stamp we can work with.
								String votetime = tvoter.get("vote_time").toString() + "000";
								String voteday = "";
								String svotetime = "";
								try {
									long realvotetime = Long.parseLong(votetime);
									Date votedate = new Date(realvotetime);
									voteday = date.format(votedate);
									svotetime = time.format(votedate);
								}catch(NumberFormatException e) {
									
								}
								
								String[] signdata = plugin.cachedItems.getSignData(playername, voteday, HeadLocation.Type.RecentVoter, i, svotetime);
								HeadData hd = new HeadData(playername, signdata, HeadLocation.Type.RecentVoter, i);
								plugin.headdata.setHead(hd, false);
								i++;
							}
						}
						Bukkit.getServer().getPluginManager().callEvent(new HeadsUpdatedEvent(HeadLocation.Type.RecentVoter));
					}
				}
				if(array.get("top_players") instanceof JSONArray) {
					JSONArray topplayers = (JSONArray)array.get("top_players");
					if(topplayers != null) {
						plugin.headdata.clearHeadData(HeadLocation.Type.TopPlayer);
						int i = 0;
						for (Object voter : topplayers) {
							if (voter instanceof JSONObject) {
								JSONObject tvoter = (JSONObject) voter;
								String playername = (String) tvoter.get("player_name");
								String hours = tvoter.get("hours").toString();
								String[] signdata = plugin.cachedItems.getSignData(playername, "", HeadLocation.Type.TopPlayer, i, hours);
								HeadData hd = new HeadData(playername, signdata, HeadLocation.Type.TopPlayer, i);
								plugin.headdata.setHead(hd, false);
								i++;
							}
						}
						Bukkit.getServer().getPluginManager().callEvent(new HeadsUpdatedEvent(HeadLocation.Type.TopPlayer));
					}
				}
				if(array.get("top_posters") instanceof JSONArray) {
					JSONArray topplayers = (JSONArray)array.get("top_posters");
					if(topplayers != null) {
						plugin.headdata.clearHeadData(HeadLocation.Type.TopPoster);
						int i = 0;
						for (Object voter : topplayers) {
							if (voter instanceof JSONObject) {
								JSONObject tvoter = (JSONObject) voter;
								String playername = (String) tvoter.get("player_name");
								String posts = tvoter.get("posts").toString();
								String[] signdata = plugin.cachedItems.getSignData(playername, "", HeadLocation.Type.TopPoster, i, posts);
								HeadData hd = new HeadData(playername, signdata, HeadLocation.Type.TopPoster, i);
								plugin.headdata.setHead(hd, false);
								i++;
							}
						}
						Bukkit.getServer().getPluginManager().callEvent(new HeadsUpdatedEvent(HeadLocation.Type.TopPoster));
					}
				}
				if(array.get("top_forum_likes") instanceof JSONArray) {
					JSONArray topplayers = (JSONArray)array.get("top_forum_likes");
					if(topplayers != null) {
						plugin.headdata.clearHeadData(HeadLocation.Type.TopLikes);
						int i = 0;
						for (Object voter : topplayers) {
							if (voter instanceof JSONObject) {
								JSONObject tvoter = (JSONObject) voter;
								String playername = (String) tvoter.get("player_name");
								String likes = tvoter.get("likes").toString();
								String[] signdata = plugin.cachedItems.getSignData(playername, "", HeadLocation.Type.TopLikes, i, likes);
								HeadData hd = new HeadData(playername, signdata, HeadLocation.Type.TopLikes, i);
								plugin.headdata.setHead(hd, false);
								i++;
							}
						}
						Bukkit.getServer().getPluginManager().callEvent(new HeadsUpdatedEvent(HeadLocation.Type.TopLikes));
					}
				}
				if(array.get("latest_members") instanceof JSONArray) {
					JSONArray recentvoters = (JSONArray)array.get("latest_members");
					if(recentvoters != null) {
						plugin.headdata.clearHeadData(HeadLocation.Type.LatestMembers);
						int i = 0;
						for (Object voter : recentvoters) {
							if (voter instanceof JSONObject) {
								JSONObject tvoter = (JSONObject) voter;
								String playername = (String) tvoter.get("player_name");
								//We need to convert from Unix time stamp to a date stamp we can work with.
								String votetime = tvoter.get("datejoined").toString() + "000";
								String voteday = "";
								String svotetime = "";
								try {
									long realvotetime = Long.parseLong(votetime);
									Date votedate = new Date(realvotetime);
									voteday = date.format(votedate);
									svotetime = time.format(votedate);
								}catch(NumberFormatException e) {
									
								}
								
								String[] signdata = plugin.cachedItems.getSignData(playername, voteday, HeadLocation.Type.LatestMembers, i, svotetime);
								HeadData hd = new HeadData(playername, signdata, HeadLocation.Type.LatestMembers, i);
								plugin.headdata.setHead(hd, false);
								i++;
							}
						}
						Bukkit.getServer().getPluginManager().callEvent(new HeadsUpdatedEvent(HeadLocation.Type.LatestMembers));
					}
				}
				if(sender != null) {
					sender.sendMessage(ChatColor.GREEN + "Player head data successfully synched!");
				}
			} catch (ParseException e) {
				if(sender != null) {
					sender.sendMessage(ChatColor.DARK_RED + "There was an error parsing the head data.");
				}
				e.printStackTrace();
			}
		} catch (SocketTimeoutException e) {
			if(sender != null) {
				sender.sendMessage(ChatColor.DARK_RED + "There was an error connecting to enjin, please try again later.");
			}
		} catch (Throwable t) {
			if(sender != null) {
				sender.sendMessage(ChatColor.DARK_RED + "There was an error syncing the heads, please fill out a support ticket at http://enjin.com/support and include the results of your /enjin report");
			}
			t.printStackTrace();
		}
	}

	@Override
	public synchronized void run() {
		updateHeads();
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
		return new URL((EnjinMinecraftPlugin.usingSSL ? "https" : "http") + EnjinMinecraftPlugin.apiurl + "minecraft-stats");
	}

	private URL getItemsUrl() throws Throwable {
		return new URL((EnjinMinecraftPlugin.usingSSL ? "https" : "http") + EnjinMinecraftPlugin.apiurl + "m-shopping-items");
	}

	private String encode(String in) throws UnsupportedEncodingException {
		return URLEncoder.encode(in, "UTF-8");
		//return in;
	}
}
