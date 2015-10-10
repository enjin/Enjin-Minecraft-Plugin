package com.enjin.officialplugin.sync.data;

import com.enjin.core.Enjin;
import com.enjin.officialplugin.EnjinMinecraftPlugin;
import com.enjin.rpc.mappings.mappings.plugin.data.NotificationData;

import java.io.BufferedInputStream;

public class Packet1FNotifications {
    public static void handle(BufferedInputStream in, EnjinMinecraftPlugin plugin) {
        //
    }

    public static void handle(NotificationData data) {
        Enjin.getPlugin().getInstructionHandler().notify(data.getPlayers(), data.getMessage(), data.getTime());
    }
}
