package com.enjin.officialplugin.shop;

import java.util.concurrent.ConcurrentHashMap;

import com.enjin.officialplugin.heads.HeadLocation;

public class ShopItems {
	
	private static String latestpurchase = "New Purchase #";
	private static String latestitempurchase = "Item Purchase ";
	private static String latestvoter = "Latest Voter #";
	private static String topplayer = "Top Player #";
	private static String topposter = "Top Poster #";
	private static String toplikes = "Top Likes #";
	private static String latestmembers = "Latest Member ";
	
	ConcurrentHashMap<String, ShopItem> shopitems = new ConcurrentHashMap<String, ShopItem>();
	
	public ShopItems() {
		shopitems.put("multiple items", new ShopItem("Multiple Items", "Multiple Items", "", ""));
	}
	
	public void addShopItem(ShopItem item) {
		shopitems.put(item.getId().toLowerCase(), item);
	}
	
	public ShopItem getShopItem(String itemId) {
		return shopitems.get(itemId.toLowerCase());
	}
	
	public void clearShopItems() {
		shopitems.clear();
		//Always have the multiple items shop item in the list.
		shopitems.put("multiple items", new ShopItem("Multiple Items", "Multiple Items", "", ""));
	}
	
	public String[] getSignData(String player, String itemid, HeadLocation.Type type, int position, String amount) {
		String[] signdata = new String[4];
		switch(type) {
		case RecentDonator:
			signdata[0] = latestpurchase + (position + 1);
			signdata[1] = player;
			ShopItem si = getShopItem(itemid);
			if(si == null) {
				signdata[2] = "";
				signdata[3] = amount;
			}else {
				signdata[2] = si.getName();
				if(signdata[2].length() > 15) {
					signdata[2] = signdata[2].substring(0, 15);
				}
				signdata[3] = amount;
			}
			break;
		case RecentItemDonator:
			signdata[0] = latestitempurchase  + (position + 1);
			signdata[1] = player;
			si = getShopItem(itemid);
			if(si == null) {
				signdata[2] = "";
				signdata[3] = "";
			}else {
				signdata[2] = si.getName();
				if(signdata[2].length() > 15) {
					signdata[2] = signdata[2].substring(0, 15);
				}
				signdata[3] = amount;
			}
			break;
		case RecentVoter:
			signdata[0] = latestvoter + (position + 1);
			signdata[1] = player;
			signdata[2] = itemid;
			signdata[3] = amount;
			break;
		case TopMonthlyVoter:
			signdata[0] = "#" + (position + 1) + " Top Monthly";
			signdata[1] = "Voter";
			signdata[2] = player;
			signdata[3] = amount + " Votes";
			break;
		case TopWeeklyVoter:
			signdata[0] = "#" + (position + 1) + " Top Weekly";
			signdata[1] = "Voter";
			signdata[2] = player;
			signdata[3] = amount + " Votes";
			break;
		case TopDailyVoter:
			signdata[0] = "#" + (position + 1) + " Top Daily";
			signdata[1] = "Voter";
			signdata[2] = player;
			signdata[3] = amount + " Votes";
			break;
		case TopPlayer: 
			signdata[0] = topplayer + (position + 1);
			signdata[1] = player;
			signdata[2] = "";
			signdata[3] = amount + " Hours";
			break;
		case TopPoster:
			signdata[0] = topposter + (position + 1);
			signdata[1] = player;
			signdata[2] = "";
			signdata[3] = amount + " Posts";
			break;
		case TopLikes:
			signdata[0] = toplikes + (position + 1);
			signdata[1] = player;
			signdata[2] = "";
			signdata[3] = amount + " Likes";
			break;
		case LatestMembers:
			signdata[0] = latestmembers + (position + 1);
			signdata[1] = player;
			signdata[2] = itemid;
			signdata[3] = amount;
			break;
		}
		return signdata;
	}
	
	public String[] updateSignData(String[] signdata, HeadLocation.Type type, int position) {
		switch(type) {
		case RecentDonator:
			signdata[0] = latestpurchase + (position + 1);
			break;
		case RecentItemDonator:
			signdata[0] = latestitempurchase  + (position + 1);
			break;
		case RecentVoter:
			signdata[0] = latestvoter + (position + 1);
			break;
		case TopMonthlyVoter:
			signdata[0] = "#" + (position + 1) + " Top Monthly";
			signdata[1] = "Voter";
			break;
		case TopWeeklyVoter:
			signdata[0] = "#" + (position + 1) + " Top Weekly";
			signdata[1] = "Voter";
			break;
		case TopDailyVoter:
			signdata[0] = "#" + (position + 1) + " Top Daily";
			signdata[1] = "Voter";
			break;
		case TopPlayer: 
			signdata[0] = topplayer + (position + 1);
			break;
		case TopPoster:
			signdata[0] = topposter + (position + 1);
			break;
		case TopLikes:
			signdata[0] = toplikes + (position + 1);
			break;
		case LatestMembers:
			signdata[0] = latestmembers + (position + 1);
			break;
		}
		return signdata;
	}

}
