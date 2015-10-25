package com.enjin.bukkit.sync.data;

import com.enjin.core.Enjin;
import com.enjin.rpc.mappings.mappings.plugin.data.PlayerGroupUpdateData;

public class AddPlayerGroupInstruction {
    public static void handle(PlayerGroupUpdateData data) {
        Enjin.getPlugin().getInstructionHandler().addToGroup(data.getPlayer(), data.getGroup(), data.getWorld());
    }
}
