package com.enjin.officialplugin.sync.data;

import com.enjin.core.Enjin;

public class BanPlayersInstruction {
    public static void handle(String player) {
        Enjin.getPlugin().getInstructionHandler().ban(player);
    }
}
