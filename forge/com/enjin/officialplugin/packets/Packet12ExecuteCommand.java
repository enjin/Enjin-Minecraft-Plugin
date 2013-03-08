package com.enjin.officialplugin.packets;

import java.io.BufferedInputStream;

import net.minecraft.server.MinecraftServer;

import com.enjin.officialplugin.EnjinMinecraftPlugin;
import com.enjin.officialplugin.threaded.CommandExecuter;

/**
 * 
 * @author OverCaste (Enjin LTE PTD).
 * This software is released under an Open Source license.
 * @copyright Enjin 2012.
 * 
 */

public class Packet12ExecuteCommand {

	public static void handle(BufferedInputStream in, EnjinMinecraftPlugin plugin) {
		try {
			String command = PacketUtilities.readString(in);
			plugin.debug("Executing command \"" + command + "\" as console.");
			plugin.scheduler.scheduleSyncDelayedTask(new CommandExecuter(null, command));
			//Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), PacketUtilities.readString(in));
		} catch (Throwable t) {
			MinecraftServer.logger.warning("Failed to dispatch command via 0x12, " + t.getMessage());
			t.printStackTrace();
		}
	}
}
