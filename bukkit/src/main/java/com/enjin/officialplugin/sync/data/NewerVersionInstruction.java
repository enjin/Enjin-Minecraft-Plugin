package com.enjin.officialplugin.sync.data;

import com.enjin.core.Enjin;

public class NewerVersionInstruction {
    public static void handle(String version) {
        Enjin.getPlugin().getInstructionHandler().version(version);
    }
}