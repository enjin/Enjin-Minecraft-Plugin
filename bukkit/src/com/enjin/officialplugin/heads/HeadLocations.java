package com.enjin.officialplugin.heads;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;

/**
 * This stores a map of all the head locations and where they are.
 * @author Tux2
 *
 */
public class HeadLocations {
	
	ConcurrentHashMap<HeadLocation.Type, ArrayList<HeadLocation>> headlist = new ConcurrentHashMap<HeadLocation.Type, ArrayList<HeadLocation>>();
	ConcurrentHashMap<String, HeadLocation> locheadlist = new ConcurrentHashMap<String, HeadLocation>();
	
	/**
	 * Adds a new location. Please note that this does not save the heads.
	 * Make sure to call the method {@link #saveHeads()} after adding a head.
	 * @param head The head location to add.
	 */
	public void addHead(HeadLocation head) {
		ArrayList<HeadLocation> heads = headlist.get(head.getType());
		if(heads == null) {
			heads = new ArrayList<HeadLocation>();
			headlist.put(head.getType(), heads);
		}
		heads.add(head);
		locheadlist.put(signLocationToString(head), head);
		if(head.hasHead()) {
			locheadlist.put(headLocationToString(head), head);
		}
	}
	
	/**
	 * Removes the {@link HeadLocation} associated with that location.
	 * Please note that this does not save the heads.
	 * Make sure to call the method {@link #saveHeads()} after adding a head.
	 * @param loc
	 */
	public void removeHead(Location loc) {
		HeadLocation head = locheadlist.get(locationToString(loc));
		if(head != null) {
			ArrayList<HeadLocation> heads = headlist.get(head.getType());
			if(heads != null) {
				heads.remove(head);
			}
			locheadlist.remove(signLocationToString(head));
			if(head.hasHead()) {
				locheadlist.remove(headLocationToString(head));
			}
		}
	}
	
	/**
	 * Gets an array of all locations for a specific head type.
	 * @param type The type of head to return.
	 * @return A list of heads.
	 */
	public ArrayList<HeadLocation> getHeads(HeadLocation.Type type) {
		ArrayList<HeadLocation> heads = headlist.get(type);
		if(heads == null) {
			return new ArrayList<HeadLocation>();
		}else {
			return heads;
		}
	}
	
	/**
	 * Does this location already contain a head sign/head?
	 * @param loc The location to check.
	 * @return True if this position is a registered head/sign, false otherwise.
	 */
	public boolean hasHeadHere(Location loc) {
		return locheadlist.containsKey(locationToString(loc));
	}
	
	/**
	 * This returns a location as a string representation.
	 * @param loc
	 * @return
	 */
	public String locationToString(Location loc) {
		try {
			return loc.getWorld().getName() + "." + loc.getBlockX() + "." + loc.getBlockY() + "." + loc.getBlockZ();
		}catch(NullPointerException e) {
			return "";
		}
	}
	
	public String headLocationToString(HeadLocation head) {
		return head.getWorld() + "." + head.getHeadx() + "." + head.getHeady() + "." + head.getHeadz();
	}
	
	public String signLocationToString(HeadLocation sign) {
		return sign.getWorld() + "." + sign.getSignx() + "." + sign.getSigny() + "." + sign.getSignz();
	}
	
	/**
	 * Loads all the heads from the disk.
	 */
	public void loadHeads() {
		headlist.clear();
		locheadlist.clear();
		File dataFolder = Bukkit.getServer().getPluginManager().getPlugin("Enjin Minecraft Plugin").getDataFolder();
		File headsfile = new File(dataFolder, "heads.yml");
		YamlConfiguration headsconfig = new YamlConfiguration();
		try {
			headsconfig.load(headsfile);
			ConfigurationSection headsection = headsconfig.getConfigurationSection("heads");
			if(headsection != null) {
				Set<String> keys = headsection.getValues(false).keySet();
				for(String key : keys) {
					ConfigurationSection theheads = headsection.getConfigurationSection(key);
					HeadLocation.Type type = HeadLocation.Type.valueOf(key);
					if(theheads != null) {
						Set<String> theheadids = theheads.getValues(false).keySet();
						for(String headid : theheadids) {
							boolean hashead = theheads.getBoolean(headid + ".hashead", false);
							String world = theheads.getString(headid + ".world", "world");
							int signx = theheads.getInt(headid + ".signx", 0);
							int signy = theheads.getInt(headid + ".signy", 0);
							int signz = theheads.getInt(headid + ".signz", 0);
							int position = theheads.getInt(headid + ".position", 0);
							HeadLocation hl;
							if(hashead) {
								int headx = theheads.getInt(headid + ".headx", 0);
								int heady = theheads.getInt(headid + ".heady", 0);
								int headz = theheads.getInt(headid + ".headz", 0);
								hl = new HeadLocation(world, headx, heady, headz, signx, signy, signz, type, position);
								//addHead(hl);
							}else {
								hl = new HeadLocation(world, signx, signy, signz, type, position);
							}
							if(type == HeadLocation.Type.RecentItemDonator) {
								String itemid = theheads.getString(headid + ".itemid", "");
								hl.setItemid(itemid);
							}
							addHead(hl);
						}
					}
				}
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
		} catch (IOException e) {
			// TODO Auto-generated catch block
		} catch (InvalidConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * Saves all the heads to the disk.
	 */
	public void saveHeads() {
		File dataFolder = Bukkit.getServer().getPluginManager().getPlugin("Enjin Minecraft Plugin").getDataFolder();
		File headsfile = new File(dataFolder, "heads.yml");
		YamlConfiguration headsconfig = new YamlConfiguration();
		Enumeration<HeadLocation> theheads = locheadlist.elements();
		int headid = 0;
		while(theheads.hasMoreElements()) {
			HeadLocation head = theheads.nextElement();
			headsconfig.set("heads." + head.getType().toString() + "." + headid + ".hashead", head.hasHead());
			headsconfig.set("heads." + head.getType().toString() + "." + headid + ".world", head.getWorld());
			if(head.hasHead()) {
				headsconfig.set("heads." + head.getType().toString() + "." + headid + ".headx", head.getHeadx());
				headsconfig.set("heads." + head.getType().toString() + "." + headid + ".heady", head.getHeady());
				headsconfig.set("heads." + head.getType().toString() + "." + headid + ".headz", head.getHeadz());
			}
			headsconfig.set("heads." + head.getType().toString() + "." + headid + ".signx", head.getSignx());
			headsconfig.set("heads." + head.getType().toString() + "." + headid + ".signy", head.getSigny());
			headsconfig.set("heads." + head.getType().toString() + "." + headid + ".signz", head.getSignz());
			headsconfig.set("heads." + head.getType().toString() + "." + headid + ".position", head.getPosition());
			if(head.getType() == HeadLocation.Type.RecentItemDonator) {
				headsconfig.set("heads." + head.getType().toString() + "." + headid + ".itemid", head.getItemid());
			}
			headid++;
		}
		try {
			headsconfig.save(headsfile);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
