package com.enjin.bukkit.sync.data;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import com.enjin.core.Enjin;

public class CommandsReceivedInstruction {

    private static final Collection NULL_SINGLETON_COLLECTION = Collections.singleton(null);

    public static void handle(ArrayList<Long> confirmed) {
        if (confirmed == null) {
            return;
        }

        confirmed.removeAll(NULL_SINGLETON_COLLECTION);
        Enjin.getPlugin().getInstructionHandler().commandConfirmed(confirmed);
    }
}
