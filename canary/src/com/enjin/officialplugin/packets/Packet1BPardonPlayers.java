package com.enjin.officialplugin.packets;

import java.io.BufferedInputStream;
import java.io.IOException;

import net.canarymod.Canary;

import com.enjin.officialplugin.EnjinMinecraftPlugin;
import com.enjin.officialplugin.events.EnjinPardonPlayerEvent;

/**
 * 
 * @author OverCaste (Enjin LTE PTD).
 * This software is released under an Open Source license.
 * @copyright Enjin 2012.
 * 
 */

public class Packet1BPardonPlayers {
	
	public static void handle(BufferedInputStream in, EnjinMinecraftPlugin plugin) {
		try {
			String players = PacketUtilities.readString(in);
			EnjinMinecraftPlugin.debug("Removing these players from the banlist: " + players);
			String[] msg = players.split(",");
			EnjinPardonPlayerEvent event = new EnjinPardonPlayerEvent(msg);
			Canary.hooks().callHook(event);
			if(event.isCanceled()) {
				return;
			}
			msg = event.getPardonedPlayers();
			if((msg.length > 0)) {
				for(int i = 0; i < msg.length; i++) {
					Canary.bans().unban(msg[i]);
					plugin.banlistertask.pardonBannedPlayer(msg[i]);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
