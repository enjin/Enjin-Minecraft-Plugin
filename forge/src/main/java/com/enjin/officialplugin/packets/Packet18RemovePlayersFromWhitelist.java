package com.enjin.officialplugin.packets;

import java.io.BufferedInputStream;
import java.io.IOException;

import net.minecraft.server.MinecraftServer;

import com.enjin.officialplugin.EnjinMinecraftPlugin;
import com.enjin.officialplugin.events.RemoveWhitelistPlayersEvent;

/**
 * @author OverCaste (Enjin LTE PTD).
 *         This software is released under an Open Source license.
 * @copyright Enjin 2012.
 */

public class Packet18RemovePlayersFromWhitelist {

    public static void handle(BufferedInputStream in, EnjinMinecraftPlugin plugin) {
        try {
            String players = PacketUtilities.readString(in);
            plugin.debug("Removing these players from the whitelist: " + players);
            String[] msg = players.split(",");
            //plugin.getServer().getPluginManager().callEvent(new RemoveWhitelistPlayersEvent(msg));
            if ((msg.length > 0)) {
                for (int i = 0; i < msg.length; i++) {
                    MinecraftServer.getServer().getConfigurationManager().removeFromWhitelist(msg[i]);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
