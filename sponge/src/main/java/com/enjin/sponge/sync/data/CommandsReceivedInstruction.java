package com.enjin.sponge.sync.data;

import com.enjin.core.Enjin;

import java.util.ArrayList;

public class CommandsReceivedInstruction {
    public static void handle(ArrayList<Long> confirmed) {
        if (confirmed == null) {
            return;
        }

        Enjin.getPlugin().getInstructionHandler().commandConfirmed(confirmed);
    }
}
