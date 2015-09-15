package com.enjin.officialplugin.sync.data;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.util.UUID;

import com.enjin.officialplugin.util.PacketUtilities;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

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
            EnjinMinecraftPlugin.debug("Adding these players to the banlist: " + players);
            String[] msg = players.split(",");
            OfflinePlayer[] oplayers = new OfflinePlayer[msg.length];
            for (int i = 0; i < msg.length; i++) {
                String playername = msg[i];
                if (playername.length() == 32) {
                    // expand UUIDs which do not have dashes in them
                    playername = playername.substring(0, 8) + "-" + playername.substring(8, 12) + "-" + playername.substring(12, 16) +
                            "-" + playername.substring(16, 20) + "-" + playername.substring(20, 32);
                }
                if (playername.length() == 36) {
                    try {
                        oplayers[i] = Bukkit.getOfflinePlayer(UUID.fromString(playername));
                    } catch (Exception e) {
                        oplayers[i] = Bukkit.getOfflinePlayer(playername);
                    }
                } else {
                    oplayers[i] = Bukkit.getOfflinePlayer(playername);
                }
            }
            EnjinBanPlayerEvent event = new EnjinBanPlayerEvent(oplayers);
            plugin.getServer().getPluginManager().callEvent(event);
            if (event.isCancelled()) {
                return;
            }
            oplayers = event.getBannedPlayers();
            if ((oplayers.length > 0)) {
                for (int i = 0; i < oplayers.length; i++) {
                    plugin.banlistertask.addBannedPlayer(oplayers[i]);
                    oplayers[i].setBanned(true);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
