package com.enjin.officialplugin.sync.data;

import java.util.ArrayList;

import com.enjin.core.Enjin;

public class CommandsReceivedInstruction {
    public static void handle(ArrayList<Long> confirmed) {
        if (confirmed == null) {
            return;
        }

        Enjin.getPlugin().getInstructionHandler().commandConfirmed(confirmed);
    }
}
