package com.enjin.officialplugin.listeners;


import net.minecraftforge.event.ForgeSubscribe;
import net.minecraftforge.event.ServerChatEvent;

import com.enjin.officialplugin.EnjinMinecraftPlugin;

public class NewPlayerChatListener {
	
	EnjinMinecraftPlugin plugin;
	
	public NewPlayerChatListener(EnjinMinecraftPlugin plugin) {
		this.plugin = plugin;
	}
	
	@ForgeSubscribe
	public void playerChatEvent(ServerChatEvent event) {
		if(event.isCanceled()) {
			return;
		}
		//plugin.GetPlayerStats(event.player.username).addChatLine();
	}

}
