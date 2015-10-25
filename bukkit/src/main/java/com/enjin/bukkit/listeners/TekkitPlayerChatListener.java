package com.enjin.bukkit.listeners;

import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChatEvent;

import com.enjin.bukkit.EnjinMinecraftPlugin;

//We need to handle chat differently in tekkit as the method
//was depreciated in 1.3, and tekkit still uses 1.2.5 bukkit
@SuppressWarnings("deprecation")
public class TekkitPlayerChatListener implements Listener {

    EnjinMinecraftPlugin plugin;

    public TekkitPlayerChatListener(EnjinMinecraftPlugin plugin) {
        this.plugin = plugin;
    }

    public void playerChatEvent(PlayerChatEvent event) {
        if (event.isCancelled()) {
            return;
        }
        plugin.getPlayerStats(event.getPlayer()).addChatLine();
    }

}
