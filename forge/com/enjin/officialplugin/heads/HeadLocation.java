package com.enjin.officialplugin.heads;

import net.minecraft.server.MinecraftServer;
import net.minecraft.world.World;

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

	Location signLoc;
	Location headLoc;
	Type type;
	int position = 0;
	String itemid = "";
	
	/**
	 * Initiates a head location with just a sign.
	 * @param world The world object this location is in.
	 * @param signx The x position of the sign.
	 * @param signy The y position of the sign.
	 * @param signz The z position of the sign.
	 * @param type The type of stat that we are going to display on this sign.
	 * @param position The ranking of this sign. (0-9)
	 */
	public HeadLocation(World worldObj, int signx, int signy, int signz,
			Type type, int position) {
		signLoc = new Location(worldObj, signx, signy, signz);
		this.type = type;
		this.position = position;
	}
	
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
		signLoc = new Location(world, signx, signy, signz);
		this.type = type;
		this.position = position;
	}
	
	/**
	 * Initiates a head location with both a head and a sign.
	 * @param world The world object this location is in.
	 * @param headx The x position of the head.
	 * @param heady The y position of the head.
	 * @param headz The z position of the head.
	 * @param signx The x position of the sign.
	 * @param signy The y position of the sign.
	 * @param signz The z position of the sign.
	 * @param type The type of stat that we are going to display on this sign.
	 * @param position The ranking of this sign. (0-9)
	 */
	public HeadLocation(World worldObj, int headx, int heady, int headz,
			int signx, int signy, int signz, Type type, int position) {
		signLoc = new Location(worldObj, signx, signy, signz);
		headLoc = new Location(worldObj, headx, heady, headz);
		this.type = type;
		this.position = position;
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
		signLoc = new Location(world, signx, signy, signz);
		headLoc = new Location(world, headx, heady, headz);
		this.type = type;
		this.position = position;
	}
	
	/**
	 * Gets the location of the sign for this stat.
	 * @return The location, or null if the world isn't loaded.
	 */
	public Location getSignLocation() {
		return signLoc;
	}
	
	/**
	 * Gets the head loation for this stat.
	 * @return The location of the head, or null if the world isn't loaded.
	 */
	public Location getHeadLocation() {
		return headLoc;
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
		return headLoc != null;
	}

	public String getWorld() {
		return signLoc.getWorld();
	}
	
	public World getWorldObj() {
		return signLoc.getWorldObj();
	}

	public int getHeadx() {
		return headLoc.getX();
	}

	public int getHeady() {
		return headLoc.getY();
	}

	public int getHeadz() {
		return headLoc.getZ();
	}

	public int getSignx() {
		return signLoc.getX();
	}

	public int getSigny() {
		return signLoc.getY();
	}

	public int getSignz() {
		return signLoc.getZ();
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
