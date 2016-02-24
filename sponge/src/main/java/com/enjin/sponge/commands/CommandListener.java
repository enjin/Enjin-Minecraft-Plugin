package com.enjin.sponge.commands;

import com.enjin.core.Enjin;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.command.SendCommandEvent;

public class CommandListener {
    @Listener
    public void onSendCommand(SendCommandEvent event) {
        Enjin.getLogger().info(event.toString());
    }
}
