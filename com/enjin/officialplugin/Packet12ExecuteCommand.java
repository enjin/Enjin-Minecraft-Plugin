package com.enjin.officialplugin;

import java.io.InputStream;

import org.bukkit.Bukkit;

/**
 * 
 * @author OverCaste (Enjin LTE PTD).
 * This software is released under an Open Source license.
 * @copyright Enjin 2012.
 * 
 */

public class Packet12ExecuteCommand {

	public static void handle(InputStream in, EnjinMinecraftPlugin plugin) {
		try {
			Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new CommandExecuter(Bukkit.getConsoleSender(), PacketUtilities.readString(in)));
			//Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), PacketUtilities.readString(in));
		} catch (Throwable t) {
			Bukkit.getLogger().warning("Failed to dispatch command via 0x12, " + t.getMessage());
			t.printStackTrace();
		}
	}
}
