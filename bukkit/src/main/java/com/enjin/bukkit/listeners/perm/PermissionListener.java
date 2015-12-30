package com.enjin.bukkit.listeners.perm;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.server.ServerCommandEvent;

public abstract class PermissionListener extends PermissionProcessor implements Listener {
    @EventHandler
    public void onPlayerCommandPreprocessEvent(PlayerCommandPreprocessEvent event) {
        processCommand(event.getPlayer(), event.getMessage().replaceFirst("/", "").trim(), event);
    }

    @EventHandler
    public void onServerCommand(ServerCommandEvent event) {
        processCommand(event.getSender(), event.getCommand().trim(), event);
    }
}
