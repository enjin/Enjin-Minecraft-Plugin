package com.enjin.officialplugin.sync.data;

import com.enjin.officialplugin.EnjinMinecraftPlugin;
import com.enjin.officialplugin.utils.commands.CommandWrapper;
import com.enjin.officialplugin.utils.packet.PacketUtilities;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Packet1ECommandsReceived {
    public static void handle(BufferedInputStream in, EnjinMinecraftPlugin plugin) {
        try {
            String commandsreceived = PacketUtilities.readString(in);
            EnjinMinecraftPlugin.getInstance().debug("Removing these command ids from the list: " + commandsreceived);
            String[] ids = commandsreceived.split(",");

            List<CommandWrapper> wrappers;
            for (String id : ids) {
                wrappers = new ArrayList<CommandWrapper>(EnjinMinecraftPlugin.getProcessedCommands());

                for (CommandWrapper wrapper : wrappers) {
                    if (wrapper.getId().equalsIgnoreCase(id)) {
                        EnjinMinecraftPlugin.getProcessedCommands().remove(wrapper);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
