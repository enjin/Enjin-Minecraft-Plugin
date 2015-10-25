package com.enjin.bukkit.sync.data;

import com.enjin.core.Enjin;

public class PardonPlayersInstruction {
    public static void handle(String player) {
        Enjin.getPlugin().getInstructionHandler().pardon(player);
    }
}
