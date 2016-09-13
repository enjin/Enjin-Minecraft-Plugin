package com.enjin.bukkit.listeners;

import com.enjin.bukkit.EnjinMinecraftPlugin;
import com.enjin.bukkit.modules.impl.StatsModule;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class ChatListener implements Listener {
    @EventHandler(priority = EventPriority.MONITOR)
    public void playerChatEvent(AsyncPlayerChatEvent event) {
        if (event.isCancelled()) {
            return;
        }

        StatsModule module = EnjinMinecraftPlugin.getInstance().getModuleManager().getModule(StatsModule.class);
        if (module != null) {
            module.getPlayerStats(event.getPlayer()).addChatLine();
        }
    }
}
