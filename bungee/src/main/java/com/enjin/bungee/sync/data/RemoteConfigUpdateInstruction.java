package com.enjin.bungee.sync.data;

import com.enjin.core.Enjin;

import java.util.Map;

public class RemoteConfigUpdateInstruction {
    public static void handle(Map<String, Object> updates) {
        Enjin.getPlugin().getInstructionHandler().configUpdated(updates);
    }
}
