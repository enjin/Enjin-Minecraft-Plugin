package com.enjin.sponge.sync.data;

import com.enjin.core.Enjin;
import com.enjin.rpc.mappings.mappings.plugin.data.ExecuteData;
import com.google.common.base.Optional;

public class ExecuteCommandInstruction {
    public static void handle(ExecuteData data) {
        Enjin.getPlugin()
             .getInstructionHandler()
             .execute(data.getId(),
                      data.getCommand(),
                      Optional.fromNullable(data.getDelay()),
                      Optional.fromNullable(data.getRequireOnline()),
                      Optional.fromNullable(data.getPlayer()),
                      Optional.fromNullable(data.getUuid()));
    }
}
