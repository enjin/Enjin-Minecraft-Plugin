package com.enjin.sponge.sync.data;

import com.enjin.core.Enjin;
import com.enjin.rpc.mappings.mappings.plugin.data.PlayerGroupUpdateData;

public class RemovePlayerGroupInstruction {
    public static void handle(PlayerGroupUpdateData data) {
        Enjin.getPlugin().getInstructionHandler().removeFromGroup(data.getPlayer(), data.getGroup(), data.getWorld());
    }
}
