package com.enjin.officialplugin;

import java.io.InputStream;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

/**
 * 
 * @author OverCaste (Enjin LTE PTD).
 * This software is released under an Open Source license.
 * @copyright Enjin 2012.
 * 
 */

public class Packet13ExecuteCommandAsPlayer {
	
	public static void handle(InputStream in) {
		try {
			String name = PacketUtilities.readString(in);
			String command = PacketUtilities.readString(in);
			Player p = Bukkit.getPlayerExact(name);
			if(p == null) {
				return;
			}
			Bukkit.getServer().dispatchCommand(p, command);
		} catch (Throwable t) {
			Bukkit.getLogger().warning("Failed to dispatch command via 0x13, " + t.getMessage());
			t.printStackTrace();
		}
	}
}
