package com.enjin.officialplugin.heads;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

/**
 * This stores everything about a specific head location, including the sign
 * location, and optionally, a head location if there is a head associated
 * with the sign.
 * @author Tux2
 *
 */
public class HeadLocation {
	
	public enum Type {
		TopDailyVoter,
		TopWeeklyVoter,
		TopMonthlyVoter,
		RecentVoter,
		RecentDonator,
		RecentItemDonator,
		TopPlayer,
		TopPoster,
		TopLikes,
		LatestMembers,
		TopPoints,
		TopPointsMonth,
		TopPointsWeek,
		TopPointsDay,
		TopDonators,
		TopDonatorsDay,
		TopDonatorsWeek,
		TopDonatorsMonth,
		TopPointsDonators,
		TopPointsDonatorsDay,
		TopPointsDonatorsWeek,
		TopPointsDonatorsMonth;
	}

	boolean hashead = true;
	String world = "";
	int headx = 0;
	int heady = 0;
	int headz = 0;
	int signx = 0;
	int signy = 0;
	int signz = 0;
	Type type = Type.RecentItemDonator;
	int position = 0;
	String itemid = "";
	
	/**
	 * Initiates a head location with just a sign.
	 * @param world The world name this location is in.
	 * @param signx The x position of the sign.
	 * @param signy The y position of the sign.
	 * @param signz The z position of the sign.
	 * @param type The type of stat that we are going to display on this sign.
	 * @param position The ranking of this sign. (0-9)
	 */
	public HeadLocation(String world, int signx, int signy, int signz,
			Type type, int position) {
		this.world = world;
		this.signx = signx;
		this.signy = signy;
		this.signz = signz;
		this.type = type;
		this.position = position;
		hashead = false;
	}

	/**
	 * Initiates a head location with both a head and a sign.
	 * @param world The world name this location is in.
	 * @param headx The x position of the head.
	 * @param heady The y position of the head.
	 * @param headz The z position of the head.
	 * @param signx The x position of the sign.
	 * @param signy The y position of the sign.
	 * @param signz The z position of the sign.
	 * @param type The type of stat that we are going to display on this sign.
	 * @param position The ranking of this sign. (0-9)
	 */
	public HeadLocation(String world, int headx, int heady, int headz,
			int signx, int signy, int signz, Type type, int position) {
		this.world = world;
		this.headx = headx;
		this.heady = heady;
		this.headz = headz;
		this.signx = signx;
		this.signy = signy;
		this.signz = signz;
		this.type = type;
		this.position = position;
	}
	
	/**
	 * Gets the location of the sign for this stat.
	 * @return The location, or null if the world isn't loaded.
	 */
	public Location getSignLocation() {
		World wworld = Bukkit.getServer().getWorld(world);
		if(world == null) {
			return null;
		}
		return  new Location(wworld, signx, signy, signz);
	}
	
	/**
	 * Gets the head loation for this stat.
	 * @return The location of the head, or null if the world isn't loaded.
	 */
	public Location getHeadLocation() {
		World wworld = Bukkit.getServer().getWorld(world);
		if(world == null) {
			return null;
		}
		return new Location(wworld, headx, heady, headz);
	}
	
	/**
	 * Gets the type of stat this location is for.
	 * @return
	 */
	public Type getType() {
		return type;
	}
	
	/**
	 * Gets the ranking this head is for.
	 * @return 0-9, 0 = highest, 9 = lowest.
	 */
	public int getPosition() {
		return position;
	}
	
	/**
	 * If this particular one has a head location as well as a sign location.
	 * @return True if there is a head, false if it's just a sign location.
	 */
	public boolean hasHead() {
		return hashead;
	}

	public String getWorld() {
		return world;
	}

	public int getHeadx() {
		return headx;
	}

	public int getHeady() {
		return heady;
	}

	public int getHeadz() {
		return headz;
	}

	public int getSignx() {
		return signx;
	}

	public int getSigny() {
		return signy;
	}

	public int getSignz() {
		return signz;
	}

	public String getItemid() {
		return itemid;
	}

	/**
	 * Sets the item ID for the Donation for an Item
	 * @param itemid
	 */
	public void setItemid(String itemid) {
		this.itemid = itemid;
	}
}
