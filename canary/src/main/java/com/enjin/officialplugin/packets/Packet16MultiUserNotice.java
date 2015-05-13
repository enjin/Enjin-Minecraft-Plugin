package com.enjin.officialplugin.packets;

import java.io.BufferedInputStream;

import net.canarymod.Canary;
import net.canarymod.api.entity.living.humanoid.Player;

import com.enjin.officialplugin.EnjinConsole;
import com.enjin.officialplugin.EnjinMinecraftPlugin;

/**
 * @author OverCaste (Enjin LTE PTD).
 *         This software is released under an Open Source license.
 * @copyright Enjin 2012.
 */

public class Packet16MultiUserNotice {

    public static void handle(BufferedInputStream in, EnjinMinecraftPlugin plugin) {
        try {
            String players = PacketUtilities.readString(in);
            String message = PacketUtilities.readString(in);
            EnjinMinecraftPlugin.debug("Sending the following message to these users: " + players);
            EnjinMinecraftPlugin.debug(message);
            message = EnjinConsole.translateColorCodes(message);
            String[] splitvalues = players.split(",");
            for (String playerstring : splitvalues) {
                Player player = Canary.getServer().getPlayer(playerstring);
                if (player != null) {
                    player.sendMessage(message);
                }
            }
        } catch (Throwable t) {
            EnjinMinecraftPlugin.logger.warning("Failed to send message to players., " + t.getMessage());
            t.printStackTrace();
        }
    }
}
