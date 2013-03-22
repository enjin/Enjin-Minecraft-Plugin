package com.enjin.officialplugin.packets;

import java.io.BufferedInputStream;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;

import com.enjin.officialplugin.ChatColor;
import com.enjin.officialplugin.EnjinConsole;
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
			message = EnjinConsole.translateColorCodes(message);
			String[] splitvalues = players.split(",");
			for(String playerstring : splitvalues) {
				EntityPlayerMP player = MinecraftServer.getServer().getConfigurationManager().getPlayerForUsername(playerstring);
				if(player != null) {
					player.sendChatToPlayer(message);
				}
				
			}
		} catch (Throwable t) {
			MinecraftServer.getServer().logWarning("Failed to send message to players., " + t.getMessage());
			t.printStackTrace();
		}
	}
}
