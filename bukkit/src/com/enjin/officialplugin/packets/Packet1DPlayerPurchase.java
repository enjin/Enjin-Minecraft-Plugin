package com.enjin.officialplugin.packets;

import java.io.BufferedInputStream;
import java.io.IOException;

import com.enjin.officialplugin.EnjinMinecraftPlugin;
import com.enjin.officialplugin.events.HeadsUpdatedEvent;
import com.enjin.officialplugin.heads.HeadData;
import com.enjin.officialplugin.heads.HeadLocation.Type;

/**
 * 
 * @author OverCaste (Enjin LTE PTD).
 * This software is released under an Open Source license.
 * @copyright Enjin 2012.
 * 
 */

public class Packet1DPlayerPurchase {
	
	public static void handle(BufferedInputStream in, EnjinMinecraftPlugin plugin) {
		try {
			String players = PacketUtilities.readString(in);
			EnjinMinecraftPlugin.debug("Removing these player's buffered buy lists: " + players);
			String[] msg = players.split(",");
			if((msg.length > 0)) {
				for(int i = 0; i < msg.length; i++) {
					String[] playersplit = msg[i].split(":");
					if(playersplit.length > 1) {
						if(playersplit[1].equals("")) {
							playersplit[1] = "Multiple Items";
						}
						String[] signdata = plugin.cachedItems.getSignData(playersplit[0], playersplit[1], Type.RecentDonator, 0, playersplit[2]);
						HeadData hd = new HeadData(playersplit[0], signdata, Type.RecentDonator, 0);
						plugin.headdata.addToHead(hd, true);
						
						plugin.shoplistener.removePlayer(playersplit[0]);
					}else {
						String[] signdata = plugin.cachedItems.getSignData(msg[i], "", Type.RecentDonator, 0, "");
						HeadData hd = new HeadData(playersplit[0], signdata, Type.RecentDonator, 0);
						plugin.headdata.addToHead(hd, true);
						
						plugin.shoplistener.removePlayer(msg[i]);
					}
					plugin.getServer().getPluginManager().callEvent(new HeadsUpdatedEvent(Type.RecentDonator));
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
