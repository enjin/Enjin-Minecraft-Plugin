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

public class Packet16MultiUserNotice {

	public static void handle(BufferedInputStream in, EnjinMinecraftPlugin plugin) {
		try {
			String players = PacketUtilities.readString(in);
			String message = PacketUtilities.readString(in);
			plugin.debug("Sending the following message to these users: " + players);
			plugin.debug(message);
			message = plugin.translateColorCodes(message);
			String[] splitvalues = players.split(",");
			for(String playerstring : splitvalues) {
				Player player = plugin.getServer().getPlayerExact(playerstring);
				if(player != null && player.isOnline()) {
					player.sendMessage(message);
				}
			}
		} catch (Throwable t) {
			Bukkit.getLogger().warning("Failed to send message to players., " + t.getMessage());
			t.printStackTrace();
		}
	}
}
