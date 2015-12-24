package com.enjin.bukkit.sync.data;

import com.enjin.core.Enjin;
import com.enjin.rpc.mappings.mappings.plugin.data.ExecuteData;

import java.util.Optional;

public class ExecuteCommandInstruction {
    public static void handle(ExecuteData data) {
        Enjin.getPlugin().getInstructionHandler().execute(data.getId(), data.getCommand(), data.getDelay(),
                Optional.ofNullable(data.getRequireOnline()),Optional.ofNullable(data.getPlayer()), Optional.ofNullable(data.getUuid()));
    }
}
