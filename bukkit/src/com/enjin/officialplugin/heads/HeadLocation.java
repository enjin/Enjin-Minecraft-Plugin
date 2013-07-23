package com.enjin.officialplugin.heads;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

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
		LatestMembers;
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
	
	public Location getSignLocation() {
		World wworld = Bukkit.getServer().getWorld(world);
		if(world == null) {
			return null;
		}
		return  new Location(wworld, signx, signy, signz);
	}
	
	public Location getHeadLocation() {
		World wworld = Bukkit.getServer().getWorld(world);
		if(world == null) {
			return null;
		}
		return new Location(wworld, headx, heady, headz);
	}
	
	public Type getType() {
		return type;
	}
	
	public int getPosition() {
		return position;
	}
	
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

	public void setItemid(String itemid) {
		this.itemid = itemid;
	}
}
