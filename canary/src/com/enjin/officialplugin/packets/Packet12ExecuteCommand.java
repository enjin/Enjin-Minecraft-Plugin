package com.enjin.officialplugin.packets;

import java.io.BufferedInputStream;

import com.enjin.officialplugin.EnjinMinecraftPlugin;

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
			EnjinMinecraftPlugin.debug("Executing command \"" + command + "\" as console.");
			plugin.commandqueue.addCommand(null, command);
			//ServerTaskManager.addTask(new CommandExecuter(null, command, plugin));
		} catch (Throwable t) {
			EnjinMinecraftPlugin.logger.warning("Failed to dispatch command via 0x12, " + t.getMessage());
			t.printStackTrace();
		}
	}
}
