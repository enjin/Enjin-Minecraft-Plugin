package com.enjin.officialplugin.packets;

import java.io.BufferedInputStream;
import java.io.IOException;

import net.canarymod.Canary;
import net.canarymod.bansystem.Ban;

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
            EnjinBanPlayerEvent event = new EnjinBanPlayerEvent(msg);
            Canary.hooks().callHook(event);
            if (event.isCanceled()) {
                return;
            }
            msg = event.getBannedPlayers();
            if ((msg.length > 0)) {
                for (int i = 0; i < msg.length; i++) {
                    Ban theban = new Ban();
                    theban.setSubject(msg[i]);
                    theban.setReason("Banned by Enjin plugin");
                    Canary.bans().issueBan(theban);
                    plugin.banlistertask.addBannedPlayer(msg[i]);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
