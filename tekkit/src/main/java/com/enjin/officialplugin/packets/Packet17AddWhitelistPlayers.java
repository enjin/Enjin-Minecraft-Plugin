package com.enjin.officialplugin.packets;

import java.io.BufferedInputStream;
import java.io.IOException;

import org.bukkit.OfflinePlayer;

import com.enjin.officialplugin.EnjinMinecraftPlugin;
import com.enjin.officialplugin.events.AddWhitelistPlayersEvent;

/**
 * @author OverCaste (Enjin LTE PTD).
 *         This software is released under an Open Source license.
 * @copyright Enjin 2012.
 */

public class Packet17AddWhitelistPlayers {

    public static void handle(BufferedInputStream in, EnjinMinecraftPlugin plugin) {
        try {
            String players = PacketUtilities.readString(in);
            EnjinMinecraftPlugin.debug("Adding these players to the whitelist: " + players);
            String[] msg = players.split(",");
            plugin.getServer().getPluginManager().callEvent(new AddWhitelistPlayersEvent(msg));
            if ((msg.length > 0)) {
                for (int i = 0; i < msg.length; i++) {
                    OfflinePlayer player = plugin.getServer().getOfflinePlayer(msg[i]);
                    player.setWhitelisted(true);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
