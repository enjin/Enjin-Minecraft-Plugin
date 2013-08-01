package com.enjin.officialplugin.heads;

import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Bukkit;

import com.enjin.officialplugin.EnjinMinecraftPlugin;
import com.enjin.officialplugin.events.HeadsUpdatedEvent;
import com.enjin.officialplugin.heads.HeadLocation.Type;

/**
 * This class stores all of the cached stats data for all the types of heads.
 * Please remember that the ranks start from 0, not 1 in the code, so if you want to get the
 * 5th stat, you will need to pass a 4.
 * @author Tux2
 *
 */
public class CachedHeadData {
	
	EnjinMinecraftPlugin plugin;

	ConcurrentHashMap<HeadLocation.Type, ConcurrentHashMap<Integer, HeadData>> headdata = new ConcurrentHashMap<HeadLocation.Type, ConcurrentHashMap<Integer,HeadData>>();
	ConcurrentHashMap<String, ConcurrentHashMap<Integer, HeadData>> itemheaddata = new ConcurrentHashMap<String, ConcurrentHashMap<Integer,HeadData>>();
	
	public CachedHeadData(EnjinMinecraftPlugin plugin) {
		this.plugin = plugin;
	}
	
	/**
	 * This function adds a head to the head of the list, pushing
	 * down the rankings of the others by 1, taking the first rank.
	 * @param type The type of head being added.
	 * @param head The specific head.
	 */
	public void addToHead(HeadData head, boolean callUpdate) {
		if(head.getType() == Type.RecentItemDonator) {
			ConcurrentHashMap<Integer, HeadData> mapdata = itemheaddata.get(head.getItemID().toLowerCase());
			//If there's nothing there, let's start adding it.
			if(mapdata == null) {
				mapdata = new ConcurrentHashMap<Integer, HeadData>();
				mapdata.put(0, head);
				itemheaddata.put(head.getItemID().toLowerCase(), mapdata);
				if(callUpdate) {
					Bukkit.getServer().getPluginManager().callEvent(new HeadsUpdatedEvent(head.getType()));
				}
				return;
			}
			//We've got to do this in reverse, otherwise we'll overwrite data we want.
			for(int i = 3; i >= 0; i--) {
				HeadData data = mapdata.get(i + 1);
				if(data != null) {
					data.incrementRanking();
					data.setSignData(plugin.cachedItems.updateSignData(data.getSigndata(), data.getType(), data.getRanking()));
					mapdata.put(i+1, data);
				}
			}
			//Lastly, let's put the newest element on top. :D
			mapdata.put(0, head);
			if(callUpdate) {
				Bukkit.getServer().getPluginManager().callEvent(new HeadsUpdatedEvent(head.getType()));
			}
		}else {
			ConcurrentHashMap<Integer, HeadData> mapdata = headdata.get(head.getType());
			//If there's nothing there, let's start adding it.
			if(mapdata == null) {
				mapdata = new ConcurrentHashMap<Integer, HeadData>();
				mapdata.put(0, head);
				headdata.put(head.getType(), mapdata);
				if(callUpdate) {
					Bukkit.getServer().getPluginManager().callEvent(new HeadsUpdatedEvent(head.getType()));
				}
				return;
			}
			//We've got to do this in reverse, otherwise we'll overwrite data we want.
			for(int i = 9; i >= 0; i--) {
				HeadData data = mapdata.get(i);
				if(data != null) {
					data.incrementRanking();
					data.setSignData(plugin.cachedItems.updateSignData(data.getSigndata(), data.getType(), data.getRanking()));
					mapdata.put(i+1, data);
				}
			}
			//Lastly, let's put the newest element on top. :D
			mapdata.put(0, head);
			if(callUpdate) {
				Bukkit.getServer().getPluginManager().callEvent(new HeadsUpdatedEvent(head.getType()));
			}
		}
	}

	
	/**
	 * This function sets a head at a certain position in the head list, replacing the current head
	 * in that spot.
	 * @param type The type of head being added.
	 * @param head The specific head.
	 */
	public void setHead(HeadData head, boolean callUpdate) {
		if(head.getType() == Type.RecentItemDonator) {
			ConcurrentHashMap<Integer, HeadData> mapdata = itemheaddata.get(head.getItemID().toLowerCase());
			//If there's nothing there, let's start adding it.
			if(mapdata == null) {
				mapdata = new ConcurrentHashMap<Integer, HeadData>();
				itemheaddata.put(head.getItemID().toLowerCase(), mapdata);
			}
			mapdata.put(head.getRanking(), head);
			if(callUpdate) {
				Bukkit.getServer().getPluginManager().callEvent(new HeadsUpdatedEvent(head.getType()));
			}
			return;
		}else {
			ConcurrentHashMap<Integer, HeadData> mapdata = headdata.get(head.getType());
			//If there's nothing there, let's start adding it.
			if(mapdata == null) {
				mapdata = new ConcurrentHashMap<Integer, HeadData>();
				headdata.put(head.getType(), mapdata);
			}
			mapdata.put(head.getRanking(), head);
			if(callUpdate) {
				Bukkit.getServer().getPluginManager().callEvent(new HeadsUpdatedEvent(head.getType()));
			}
		}
	}
	
	/**
	 * This will set the heads in order as in the list, first one going on top of the list.
	 * If the type is RecentItemDonator then all items must be the same itemID
	 * @param type
	 * @param head
	 */
	public void setHeads(HeadLocation.Type type, HeadData... head) {
		if(type == Type.RecentItemDonator) {
			ConcurrentHashMap<Integer, HeadData> mapdata = new ConcurrentHashMap<Integer, HeadData>();
			for(int i = 0; i < head.length; i++) {
				mapdata.put(i, head[i]);
			}
			itemheaddata.put(head[0].getItemID().toLowerCase(), mapdata);
		}else {
			ConcurrentHashMap<Integer, HeadData> mapdata = new ConcurrentHashMap<Integer, HeadData>();
			for(int i = 0; i < head.length; i++) {
				mapdata.put(i, head[i]);
			}
			headdata.put(type, mapdata);
		}
		Bukkit.getServer().getPluginManager().callEvent(new HeadsUpdatedEvent(type));
	}
	
	/**
	 * This gets a certain head a certain type.
	 * @param type The type of head to get.
	 * @param rank The rank of the head to get (The list starts from 0, not 1, so if you wanted the 5th rank, you would pass "4")
	 * @param itemId This can either be null or blank, unless you are getting a RecentItemDonator, then you need to specify the itemID
	 * @return The HeadData or null if the data does not exist for that head.
	 */
	public HeadData getHead(HeadLocation.Type type, int rank, String itemId) {
		if(type == Type.RecentItemDonator) {
			ConcurrentHashMap<Integer, HeadData> typelist = itemheaddata.get(itemId.toLowerCase());
			if(typelist == null) {
				return null;
			}
			return typelist.get(rank);
		}else {
			ConcurrentHashMap<Integer, HeadData> typelist = headdata.get(type);
			if(typelist == null) {
				return null;
			}
			return typelist.get(rank);
		}
	}
	
	/**
	 * This clears all the cached head data for all of the head types.
	 */
	public void clearHeadData() {
		headdata.clear();
		itemheaddata.clear();
	}
	
	/**
	 * Only clears the head data for a specific type of head.
	 * @param type The head type to clear.
	 */
	public void clearHeadData(HeadLocation.Type type) {
		if(type == Type.RecentItemDonator) {
			itemheaddata.clear();
		}else {
			headdata.remove(type);
		}
	}
}
