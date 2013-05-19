package com.enjin.officialplugin.listeners;

import net.canarymod.hook.HookHandler;
import net.canarymod.hook.player.ChatHook;
import net.canarymod.plugin.PluginListener;
import net.canarymod.plugin.Priority;

import com.enjin.officialplugin.EnjinMinecraftPlugin;

public class NewPlayerChatListener implements PluginListener {
	
	EnjinMinecraftPlugin plugin;
	
	public NewPlayerChatListener(EnjinMinecraftPlugin plugin) {
		this.plugin = plugin;
	}

	@HookHandler(priority = Priority.PASSIVE)
	public void playerChatEvent(ChatHook event) {
		if(event.isCanceled()) {
			return;
		}
		plugin.GetPlayerStats(event.getPlayer().getName()).addChatLine();
	}

}
