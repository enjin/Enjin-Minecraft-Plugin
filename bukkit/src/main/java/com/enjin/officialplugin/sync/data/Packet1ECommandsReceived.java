package com.enjin.officialplugin.sync.data;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.util.ArrayList;

import com.enjin.core.Enjin;
import com.enjin.officialplugin.EnjinMinecraftPlugin;
import com.enjin.officialplugin.util.PacketUtilities;

/**
 * @author OverCaste (Enjin LTE PTD).
 *         This software is released under an Open Source license.
 * @copyright Enjin 2012.
 */

public class Packet1ECommandsReceived {
    public static void handle(BufferedInputStream in, EnjinMinecraftPlugin plugin) {
        try {
            String commandsreceived = PacketUtilities.readString(in);
            EnjinMinecraftPlugin.debug("Removing these command ids from the list: " + commandsreceived);
            String[] msg = commandsreceived.split(",");
            if ((msg.length > 0)) {
                for (int i = 0; i < msg.length; i++) {
                    plugin.removeCommandID(msg[i].trim());
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void handle(ArrayList<Long> confirmed) {
        if (confirmed == null) {
            return;
        }

        Enjin.getPlugin().getInstructionHandler().commandConfirmed(confirmed);
    }
}
