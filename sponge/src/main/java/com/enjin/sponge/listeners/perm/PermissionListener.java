package com.enjin.sponge.listeners.perm;

import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.command.SendCommandEvent;

public abstract class PermissionListener extends PermissionProcessor {
    @Listener(order = Order.POST)
    public void onPlayerCommandPreprocessEvent(SendCommandEvent event) {
        if (event.isCancelled() || !(event.getCause().root() instanceof CommandSource)) {
            return;
        }

		String command = event.getCommand().startsWith("/") ? event.getCommand().replaceFirst("/", "") : event.getCommand();
		StringBuilder builder = new StringBuilder(command)
				.append(' ')
				.append(event.getArguments());

		processCommand((CommandSource) event.getCause().root(), builder.toString().trim().toLowerCase(), event);
    }
}
