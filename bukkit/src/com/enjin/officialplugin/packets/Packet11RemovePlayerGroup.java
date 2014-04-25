package com.enjin.officialplugin.packets;

import java.io.BufferedInputStream;
import java.io.IOException;

import org.bukkit.Bukkit;
import org.bukkit.World;

import com.enjin.officialplugin.CommandWrapper;
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
			EnjinMinecraftPlugin.debug("Read string: " + instring);
			String[] msg = instring.split(",");
			if((msg.length == 2) || (msg.length == 3)) {
				String playername = msg[0];
				String groupname = msg[1];
				String world = (msg.length == 3) ? msg[2] : null;
				if("*".equals(world)) {
					world = null;
				}
				EnjinMinecraftPlugin.debug("Removing player " + playername + " from group " + groupname + " in world " + world + " world");
				if(plugin.permissionsbukkit != null) {
					EnjinMinecraftPlugin.debug("Removing rank " + groupname + " for PermissionsBukkit for user " + playername);
					plugin.commandqueue.addCommand(new CommandWrapper(Bukkit.getConsoleSender(), "permissions player removegroup " + playername + " " + groupname, ""));
					//Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new CommandExecuter(Bukkit.getConsoleSender(), "permissions player removegroup " + playername + " " + groupname));
				}else {
					//We need some support if they want the group removed from all worlds if the plugin doesn't support global groups
					if((world != null) || (world == null && plugin.supportsglobalgroups)) {
						if(!EnjinMinecraftPlugin.permission.playerRemoveGroup(world, playername, groupname)) {
							Bukkit.getLogger().warning("Failed to update " + playername + "'s group.");
						}
					}else {
						for(World w : Bukkit.getWorlds()) {
							if(!EnjinMinecraftPlugin.permission.playerRemoveGroup(w.getName(), playername, groupname)) {
								Bukkit.getLogger().warning("Failed to update " + playername + "'s group.");
							}
						}
					}
				}
				plugin.getServer().getPluginManager().callEvent(new RemovePlayerGroupEvent(playername, groupname, world));
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
