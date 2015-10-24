package com.enjin.officialplugin.sync.data;

import com.enjin.core.Enjin;
import com.enjin.rpc.mappings.mappings.plugin.data.ExecuteData;

public class ExecuteCommandInstruction {
    public static void handle(ExecuteData data) {
        Enjin.getPlugin().getInstructionHandler().execute(data.getId(), data.getCommand(), data.getDelay());
    }
}
