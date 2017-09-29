package com.enjin.sponge.sync.data;

import com.enjin.core.Enjin;
import com.enjin.rpc.mappings.mappings.plugin.data.NotificationData;

public class NotificationsInstruction {
    public static void handle(NotificationData data) {
        Enjin.getPlugin().getInstructionHandler().notify(data.getPlayers(), data.getMessage(), data.getTime());
    }
}
