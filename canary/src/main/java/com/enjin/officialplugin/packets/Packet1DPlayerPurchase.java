package com.enjin.officialplugin.packets;

import java.io.BufferedInputStream;
import java.io.IOException;

import com.enjin.officialplugin.EnjinMinecraftPlugin;

/**
 * @author OverCaste (Enjin LTE PTD).
 *         This software is released under an Open Source license.
 * @copyright Enjin 2012.
 */

public class Packet1DPlayerPurchase {

    public static void handle(BufferedInputStream in, EnjinMinecraftPlugin plugin) {
        try {
            String players = PacketUtilities.readString(in);
            EnjinMinecraftPlugin.debug("Removing these player's buffered buy lists: " + players);
            String[] msg = players.split(",");
            if ((msg.length > 0)) {
                for (int i = 0; i < msg.length; i++) {
                    plugin.shoplistener.removePlayer(msg[i]);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
