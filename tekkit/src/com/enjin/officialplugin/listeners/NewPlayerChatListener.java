package com.enjin.officialplugin.listeners;

import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChatEvent;

import com.enjin.officialplugin.EnjinMinecraftPlugin;

public class NewPlayerChatListener implements Listener {
	
	EnjinMinecraftPlugin plugin;
	
	public NewPlayerChatListener(EnjinMinecraftPlugin plugin) {
		this.plugin = plugin;
	}
	
	public void playerChatEvent(PlayerChatEvent event) {
		if(event.isCancelled()) {
			return;
		}
		plugin.GetPlayerStats(event.getPlayer().getName()).addChatLine();
	}

}
