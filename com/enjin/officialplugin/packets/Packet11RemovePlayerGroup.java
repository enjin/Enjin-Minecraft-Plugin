package com.enjin.officialplugin.packets;

import java.io.BufferedInputStream;
import java.io.IOException;

import org.bukkit.Bukkit;

import com.enjin.officialplugin.EnjinMinecraftPlugin;
import com.enjin.officialplugin.events.RemovePlayerGroupEvent;

/**
 * 
 * @author OverCaste (Enjin LTE PTD).
 * This software is released under an Open Source license.
 * @copyright Enjin 2012.
 * 
 */

public class Packet11RemovePlayerGroup {
	
	public static void handle(BufferedInputStream in, EnjinMinecraftPlugin plugin) {
		try {
			String instring = PacketUtilities.readString(in);
			plugin.debug("Read string: " + instring);
			String[] msg = instring.split(",");
			if((msg.length == 2) || (msg.length == 3)) {
				String playername = msg[0];
				String groupname = msg[1];
				String world = (msg.length == 3) ? msg[2] : null;
				plugin.getServer().getPluginManager().callEvent(new RemovePlayerGroupEvent(playername, groupname, world));
				plugin.debug("Removing player " + playername + " from group " + groupname + " in world " + world + "world");
				if(!EnjinMinecraftPlugin.permission.playerRemoveGroup(world, playername, groupname)) {
					Bukkit.getLogger().warning("Failed to update " + playername + "'s group.");
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
