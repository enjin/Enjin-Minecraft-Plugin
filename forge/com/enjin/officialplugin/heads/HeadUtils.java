package com.enjin.officialplugin.heads;

import com.enjin.officialplugin.EnjinMinecraftPlugin;

import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntitySign;
import net.minecraft.tileentity.TileEntitySkull;
import net.minecraft.world.World;

/**
 * This class is full of utility methods for updating heads and signs.
 * @author Tux2
 *
 */
public class HeadUtils {
	
	
	/**
	 * This method will update a specific head location with the specified head data.
	 * @param head The location of the head/sign.
	 * @param data The data to put on the head/sign.
	 * @return False if the head/sign no longer exist, true if it was successful.
	 */
	public static boolean updateHead(HeadLocation head, HeadData data) {
		if(head.hasHead()) {
			return updateHead(head.getSignLocation(), head.getHeadLocation(), data.getPlayername(), data.getSigndata());
		}else {
			return updateSign(head.getSignLocation(), data.getSigndata());
		}
	}
	
	/**
	 * Updates the sign and head associated with it.
	 * @param signloc The location of the sign.
	 * @param headloc The location of the head.
	 * @param playername The name of the player to set the head to.
	 * @param signlines All 4 lines of the sign. Passing less than 4 will throw an exception.
	 * @return True if the update was successful. False if the head or sign is missing.
	 */
	public static boolean updateHead(Location signloc, Location headloc, String playername, String[] signlines) {
		TileEntity headblock = headloc.getWorldObj().getBlockTileEntity(headloc.getX(), headloc.getY(), headloc.getZ());
		if(headblock != null && headblock instanceof TileEntitySkull) {
			TileEntitySkull skullblock = (TileEntitySkull) headblock;
			skullblock.setSkullType(3, playername); // 3 should be the type ID for a player
			headloc.getWorldObj().markBlockForUpdate(headloc.getX(), headloc.getY(), headloc.getZ());
		}else {
			return false;
		}
		return updateSign(signloc, signlines);
	}
	
	/**
	 * A convenience method to get the Specific owner of a head.
	 * @param headlocation The location of the head.
	 * @return The name of the owner, otherwise null if there is no owner or it isn't a head block.
	 */
	public static String getHeadName(Location headlocation) {
		TileEntity headblock = headlocation.getWorldObj().getBlockTileEntity(headlocation.getX(), headlocation.getY(), headlocation.getZ());
		if(headblock != null && headblock instanceof TileEntitySkull) {
			TileEntitySkull skullblock = (TileEntitySkull) headblock;
			return skullblock.getExtraType();
		}else {
			return null;
		}
	}
	
	/**
	 * Gets the lines on a sign at a specific location.
	 * @param signloc The location of the sign.
	 * @return A String array with 4 elements for the 4 lines of the sign, otherwise null if it isn't a sign.
	 */
	public static String[] getSignData(Location signloc) {
		TileEntity sign = signloc.getWorldObj().getBlockTileEntity(signloc.getX(), signloc.getY(), signloc.getZ());
		if(sign != null && sign instanceof TileEntitySign) {
			TileEntitySign signtype = (TileEntitySign) sign;
			return signtype.signText;
		}
		return null;
	}
	
	/**
	 * Updates the sign with the data.
	 * @param signloc The location of the sign.
	 * @param lines All 4 lines of the sign. (If you pass less than 4 it will throw an exception)
	 * @return True if the update was successful. False if the head or sign is missing.
	 */
	public static boolean updateSign(Location signloc, String... lines) {
		TileEntity sign = signloc.getWorldObj().getBlockTileEntity(signloc.getX(), signloc.getY(), signloc.getZ());
		if(sign != null && sign instanceof TileEntitySign) {
			TileEntitySign signtype = (TileEntitySign) sign;
			signtype.signText[0] = lines[0];
			signtype.signText[1] = lines[1];
			signtype.signText[2] = lines[2];
			signtype.signText[3] = lines[3];
			signloc.getWorldObj().markBlockForUpdate(signloc.getX(), signloc.getY(), signloc.getZ());
			return true;
		}
		return false;
	}

	
	public static World getWorldByName(String name) {
		for (World world : MinecraftServer.getServer().worldServers) {
			if (world.getWorldInfo().getWorldName().equalsIgnoreCase(name)) {
				return world;
			}
		}
		return null;
	}
}
