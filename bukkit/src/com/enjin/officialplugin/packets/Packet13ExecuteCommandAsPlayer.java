package com.enjin.officialplugin.packets;

import java.io.BufferedInputStream;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import com.enjin.officialplugin.EnjinMinecraftPlugin;

/**
 * 
 * @author OverCaste (Enjin LTE PTD).
 * This software is released under an Open Source license.
 * @copyright Enjin 2012.
 * 
 */

public class Packet13ExecuteCommandAsPlayer {
	
	public static void handle(BufferedInputStream in, EnjinMinecraftPlugin plugin) {
		try {
			String name = PacketUtilities.readString(in);
			String command = PacketUtilities.readString(in);
			Player p = Bukkit.getPlayerExact(name);
			//TODO: Add offline player support here
			if(p == null) {
				EnjinMinecraftPlugin.debug("Failed executing command \"" + command + "\" as player " + name + ". Player isn't online.");
				return;
			}
			String[] commandsplit = command.split("\0x00");
			if(commandsplit.length > 1) {
				try {
					long time = System.currentTimeMillis() + (Long.getLong(commandsplit[1]) * 1000);
					EnjinMinecraftPlugin.debug("Executing command \"" + command + "\" as player " + name + " in " + commandsplit[1] + " seconds.");
					plugin.commexecuter.addCommand(p, commandsplit[0], time);
				}catch (NumberFormatException e) {
					EnjinMinecraftPlugin.debug("Failed to get the time on a timed command, adding as a regular command");
					plugin.commandqueue.addCommand(p, commandsplit[0]);
				}
			}else {
				EnjinMinecraftPlugin.debug("Executing command \"" + command + "\" as player " + name + ".");
				plugin.commandqueue.addCommand(p, command);
			}
		} catch (Throwable t) {
			Bukkit.getLogger().warning("Failed to dispatch command via 0x13, " + t.getMessage());
			t.printStackTrace();
		}
	}
}
