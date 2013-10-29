package com.enjin.officialplugin.heads;

import net.minecraft.world.World;

public class Location {
	
	private int x;
	private int y;
	private int z;
	private String world;
	private World worldObj;
	
	
	public Location(String world, int x, int y, int z) {
		super();
		this.world = world;
		this.x = x;
		this.y = y;
		this.z = z;
	}
	
	public Location(World worldObj, int x, int y, int z) {
		super();
		this.world = worldObj.getWorldInfo().getWorldName();
		this.worldObj = worldObj;
		this.x = x;
		this.y = y;
		this.z = z;
	}


	public int getX() {
		return x;
	}


	public int getY() {
		return y;
	}


	public int getZ() {
		return z;
	}


	public String getWorld() {
		return world;
	}
	
	public World getWorldObj() {
		if (worldObj == null) {
			return worldObj = HeadUtils.getWorldByName(world);
		} else {
			return worldObj;
		}
	}
}
