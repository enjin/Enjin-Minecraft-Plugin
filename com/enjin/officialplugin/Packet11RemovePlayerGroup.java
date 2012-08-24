package com.enjin.officialplugin;

import java.io.IOException;
import java.io.InputStream;

import org.bukkit.Bukkit;

/**
 * 
 * @author OverCaste (Enjin LTE PTD).
 * This software is released under an Open Source license.
 * @copyright Enjin 2012.
 * 
 */

public class Packet11RemovePlayerGroup {
	
	public static void handle(InputStream in) {
		try {
			String[] msg =PacketUtilities.readString(in).split(",");
			if((msg.length == 2) || (msg.length == 3)) {
				String playername = msg[0];
				String groupname = msg[1];
				String world = (msg.length == 3) ? msg[2] : null;
				if(!EnjinMinecraftPlugin.permission.playerRemoveGroup(world, playername, groupname)) {
					Bukkit.getLogger().warning("Failed to update " + playername + "'s group.");
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
