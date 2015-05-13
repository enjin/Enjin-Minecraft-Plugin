package com.enjin.officialplugin.heads;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Sign;
import org.bukkit.block.Skull;

/**
 * This class is full of utility methods for updating heads and signs.
 *
 * @author Tux2
 */
public class HeadUtils {


    /**
     * This method will update a specific head location with the specified head data.
     *
     * @param head The location of the head/sign.
     * @param data The data to put on the head/sign.
     * @return False if the head/sign no longer exist, true if it was successful.
     */
    public static boolean updateHead(HeadLocation head, HeadData data) {
        if (head.hasHead()) {
            return updateHead(head.getSignLocation(), head.getHeadLocation(), data.getPlayername(), data.getSigndata());
        } else {
            return updateSign(head.getSignLocation(), data.getSigndata());
        }
    }

    /**
     * Updates the sign and head associated with it.
     *
     * @param signloc    The location of the sign.
     * @param headloc    The location of the head.
     * @param playername The name of the player to set the head to.
     * @param signlines  All 4 lines of the sign. Passing less than 4 will throw an exception.
     * @return True if the update was successful. False if the head or sign is missing.
     */
    public static boolean updateHead(Location signloc, Location headloc, String playername, String[] signlines) {
        try {
            if (headloc == null || headloc.getBlock() == null) {
                return false;
            }
        } catch (Exception e) {
            return false;
        }
        Block headblock = headloc.getBlock();
        if (headblock != null && headblock.getType() == Material.SKULL && headblock.getState() != null) {
            //We don't want to update the head in the new craftbukkit as it HAS to be some player.
            if (!playername.equals("")) {
                Skull skullblock = (Skull) headblock.getState();
                //Only update if the name has changed!
                if (skullblock.getOwner() == null || !skullblock.getOwner().equalsIgnoreCase(playername)) {
                    skullblock.setOwner(playername);
                    skullblock.update();
                }
            }
        } else {
            return false;
        }
        return updateSign(signloc, signlines);
    }

    /**
     * A convienience method to get the Specific owner of a head.
     *
     * @param headlocation The location of the head.
     * @return The name of the owner, otherwise null if there is no owner or it isn't a head block.
     */
    public static String getHeadName(Location headlocation) {
        Block headblock = headlocation.getBlock();
        if (headblock.getState() instanceof Skull) {
            Skull skullblock = (Skull) headblock.getState();
            return skullblock.getOwner();
        } else {
            return null;
        }
    }

    /**
     * Gets the lines on a sign at a specific location.
     *
     * @param signloc The location of the sign.
     * @return A String array with 4 elements for the 4 lines of the sign, otherwise null if it isn't a sign.
     */
    public static String[] getSignData(Location signloc) {
        if (signloc == null || signloc.getBlock() == null) {
            return null;
        }
        BlockState sign = signloc.getBlock().getState();
        if (sign instanceof Sign) {
            Sign signtype = (Sign) sign;
            return signtype.getLines();
        }
        return null;
    }

    /**
     * Updates the sign with the data.
     *
     * @param signloc The location of the sign.
     * @param lines   All 4 lines of the sign. (If you pass less than 4 it will throw an exception)
     * @return True if the update was successful. False if the head or sign is missing.
     */
    public static boolean updateSign(Location signloc, String... lines) {
        try {
            if (signloc == null || signloc.getBlock() == null) {
                return false;
            }
            BlockState sign = signloc.getBlock().getState();
            if (sign instanceof Sign) {
                Sign signtype = (Sign) sign;
                signtype.setLine(0, lines[0]);
                signtype.setLine(1, lines[1]);
                signtype.setLine(2, lines[2]);
                signtype.setLine(3, lines[3]);
                signtype.update();
                return true;
            }
        } catch (Exception e) {
            return false;
        }
        return false;
    }

}
