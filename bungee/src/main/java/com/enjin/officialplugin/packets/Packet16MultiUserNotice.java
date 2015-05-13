package com.enjin.officialplugin.packets;

import java.io.BufferedInputStream;

import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import com.enjin.emp.bungee.EnjinConsole;
import com.enjin.emp.bungee.EnjinPlugin;

/**
 * @author OverCaste (Enjin LTE PTD).
 *         This software is released under an Open Source license.
 * @copyright Enjin 2012.
 */

public class Packet16MultiUserNotice {

    public static void handle(BufferedInputStream in, EnjinPlugin plugin) {
        try {
            String players = PacketUtilities.readString(in);
            String message = PacketUtilities.readString(in);
            EnjinPlugin.debug("Sending the following message to these users: " + players);
            EnjinPlugin.debug(message);
            message = EnjinConsole.translateColorCodes(message);
            String[] splitvalues = players.split(",");
            for (String playerstring : splitvalues) {
                ProxiedPlayer player = BungeeCord.getInstance().getPlayer(playerstring);
                if (player != null) {
                    player.sendMessage(message);
                }
            }
        } catch (Throwable t) {
            plugin.getLogger().warning("Failed to send message to players., " + t.getMessage());
            t.printStackTrace();
        }
    }
}
