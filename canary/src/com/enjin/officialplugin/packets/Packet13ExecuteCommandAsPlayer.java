package com.enjin.officialplugin.packets;

import java.io.BufferedInputStream;

import net.canarymod.Canary;
import net.canarymod.api.entity.living.humanoid.Player;

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
			Player p = Canary.getServer().getPlayer(name);
			//TODO: Add offline player support here
			if(p == null) {
				EnjinMinecraftPlugin.debug("Failed executing command \"" + command + "\" as player " + name + ". Player isn't online.");
				return;
			}
			EnjinMinecraftPlugin.debug("Executing command \"" + command + "\" as player " + name + ".");
			plugin.commandqueue.addCommand(p, command);
		} catch (Throwable t) {
			EnjinMinecraftPlugin.logger.warning("Failed to dispatch command via 0x13, " + t.getMessage());
			t.printStackTrace();
		}
	}
}
