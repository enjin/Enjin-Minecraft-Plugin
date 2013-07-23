package com.enjin.officialplugin.heads;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.block.Skull;

public class HeadUtils {
	
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
	 * @param signline1 Line 1 of the sign.
	 * @param signline2 Line 2 of the sign.
	 * @param signline3 Line 3 of the sign.
	 * @param signline4 Line 4 of the sign.
	 * @return True if the update was successful. False if the head or sign is missing.
	 */
	public static boolean updateHead(Location signloc, Location headloc, String playername, String[] signlines) {
		Block headblock = headloc.getBlock();
		if(headblock.getType() == Material.SKULL) {
			Skull skullblock = (Skull) headblock.getState();
			skullblock.setOwner(playername);
			skullblock.update();
		}else {
			return false;
		}
		return updateSign(signloc, signlines);
	}
	
	public static String getHeadName(Location headlocation) {
		Block headblock = headlocation.getBlock();
		if(headblock.getState() instanceof Skull) {
			Skull skullblock = (Skull) headblock.getState();
			return skullblock.getOwner();
		}else {
			return null;
		}
	}
	
	public static String[] getSignData(Location signloc) {
		BlockState sign = signloc.getBlock().getState();
		if(sign instanceof Sign) {
			Sign signtype = (Sign) sign;
			return signtype.getLines();
		}
		return null;
	}
	
	/**
	 * Updates the sign with the data.
	 * @param signloc The location of the sign.
	 * @param signline1 Line 1 of the sign.
	 * @param signline2 Line 2 of the sign.
	 * @param signline3 Line 3 of the sign.
	 * @param signline4 Line 4 of the sign.
	 * @return True if the update was successful. False if the head or sign is missing.
	 */
	public static boolean updateSign(Location signloc, String... lines) {
		BlockState sign = signloc.getBlock().getState();
		if(sign instanceof Sign) {
			Sign signtype = (Sign) sign;
			signtype.setLine(0, lines[0]);
			signtype.setLine(1, lines[1]);
			signtype.setLine(2, lines[2]);
			signtype.setLine(3, lines[3]);
			signtype.update();
			return true;
		}
		return false;
	}

}
