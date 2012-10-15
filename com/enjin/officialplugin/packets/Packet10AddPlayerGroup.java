package com.enjin.officialplugin.packets;

import java.io.BufferedInputStream;
import java.io.IOException;

import org.bukkit.Bukkit;

import com.enjin.officialplugin.EnjinMinecraftPlugin;
import com.enjin.officialplugin.events.AddPlayerGroupEvent;
import com.enjin.officialplugin.threaded.CommandExecuter;

/**
 * 
 * @author OverCaste (Enjin LTE PTD).
 * This software is released under an Open Source license.
 * @copyright Enjin 2012.
 * 
 */

public class Packet10AddPlayerGroup {
	
	public static void handle(BufferedInputStream in, EnjinMinecraftPlugin plugin) {
		try {
			String[] msg = PacketUtilities.readString(in).split(",");
			if((msg.length == 2) || (msg.length == 3)) {
				String playername = msg[0];
				String groupname = msg[1];
				String world = (msg.length == 3) ? msg[2] : null;
				if("*".equals(world)) {
					world = null;
				}
				plugin.getServer().getPluginManager().callEvent(new AddPlayerGroupEvent(playername, groupname, world));
				plugin.debug("Adding player " + playername + " from group " + groupname + " in world " + world + "world");
				//Check to see if we have PermissionsBukkit. If we do we have to do something special
				if(plugin.permissionsbukkit != null) {
					plugin.debug("Adding rank " + groupname + " for PermissionsBukkit for user " + playername);
					Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new CommandExecuter(Bukkit.getConsoleSender(), "permissions player addgroup " + playername + " " + groupname));
				}else {
					if(!EnjinMinecraftPlugin.permission.playerAddGroup(world, playername, groupname)) {
						Bukkit.getLogger().warning("Failed to update " + playername + "'s group. Please make sure that you have a valid permission plugin installed, and that your configurations are correct.");
					}
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
