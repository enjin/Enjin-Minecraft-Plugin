package com.enjin.bukkit.listeners;

import com.enjin.bukkit.EnjinMinecraftPlugin;
import com.enjin.bukkit.managers.StatsManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class NewPlayerChatListener implements Listener {

    EnjinMinecraftPlugin plugin;

    public NewPlayerChatListener(EnjinMinecraftPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void playerChatEvent(AsyncPlayerChatEvent event) {
        if (event.isCancelled()) {
            return;
        }

        StatsManager.getPlayerStats(event.getPlayer()).addChatLine();
    }

}
