package com.enjin.officialplugin.packets;

import java.io.BufferedInputStream;
import java.io.IOException;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.BanEntry;

import com.enjin.officialplugin.EnjinMinecraftPlugin;
import com.enjin.officialplugin.events.EnjinBanPlayerEvent;

/**
 * @author OverCaste (Enjin LTE PTD).
 *         This software is released under an Open Source license.
 * @copyright Enjin 2012.
 */

public class Packet1ABanPlayers {

    public static void handle(BufferedInputStream in, EnjinMinecraftPlugin plugin) {
        try {
            String players = PacketUtilities.readString(in);
            plugin.debug("Adding these players to the banlist: " + players);
            String[] msg = players.split(",");
            /*
            EnjinBanPlayerEvent event = new EnjinBanPlayerEvent(msg);
			plugin.getServer().getPluginManager().callEvent(event);
			if(event.isCancelled()) {
				return;
			}
			*/
            if ((msg.length > 0)) {
                for (int i = 0; i < msg.length; i++) {
                    BanEntry be = new BanEntry(msg[i]);
                    be.setBannedBy("Enjin");
                    be.setBanReason("Banned from website.");
                    MinecraftServer.getServer().getConfigurationManager().getBannedPlayers().put(be);
                    plugin.banlistertask.addBannedPlayer(msg[i]);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
