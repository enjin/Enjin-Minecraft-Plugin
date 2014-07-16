package com.enjin.officialplugin.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import com.enjin.officialplugin.EnjinMinecraftPlugin;

public class NewPlayerChatListener implements Listener {
	
	EnjinMinecraftPlugin plugin;
	
	public NewPlayerChatListener(EnjinMinecraftPlugin plugin) {
		this.plugin = plugin;
	}
	
	@EventHandler(priority = EventPriority.MONITOR)
	public void playerChatEvent(AsyncPlayerChatEvent event) {
		if(event.isCancelled()) {
			return;
		}
		plugin.getPlayerStats(event.getPlayer()).addChatLine();
	}

}
