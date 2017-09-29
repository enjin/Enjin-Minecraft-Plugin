package com.enjin.sponge.sync.data;

import com.enjin.core.Enjin;

public class RemoveWhitelistPlayerInstruction {
    public static void handle(String player) {
        Enjin.getPlugin().getInstructionHandler().removeFromWhitelist(player);
    }
}
