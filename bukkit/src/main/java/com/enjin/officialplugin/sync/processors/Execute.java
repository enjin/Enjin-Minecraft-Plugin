package com.enjin.officialplugin.sync.processors;

import com.enjin.officialplugin.EnjinMinecraftPlugin;
import com.enjin.rpc.mappings.mappings.plugin.data.ExecuteData;

public class Execute {
    public static void handle(ExecuteData data) {
        EnjinMinecraftPlugin.debug("Received command: id=" + data.getId() + ", command=" + data.getCommand());
    }
}
