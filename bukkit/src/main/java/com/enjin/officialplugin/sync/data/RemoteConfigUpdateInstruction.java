package com.enjin.officialplugin.sync.data;

import java.util.Map;

import com.enjin.core.Enjin;

public class RemoteConfigUpdateInstruction {
    public static void handle(Map<String, Object> updates) {
        Enjin.getPlugin().getInstructionHandler().configUpdated(updates);
    }
}
