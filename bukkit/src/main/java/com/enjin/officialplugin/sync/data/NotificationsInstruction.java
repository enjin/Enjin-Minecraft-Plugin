package com.enjin.officialplugin.sync.data;

import com.enjin.core.Enjin;
import com.enjin.officialplugin.EnjinMinecraftPlugin;
import com.enjin.rpc.mappings.mappings.plugin.data.NotificationData;

import java.io.BufferedInputStream;

public class NotificationsInstruction {
    public static void handle(NotificationData data) {
        Enjin.getPlugin().getInstructionHandler().notify(data.getPlayers(), data.getMessage(), data.getTime());
    }
}
