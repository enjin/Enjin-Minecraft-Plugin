package com.enjin.officialplugin;

import org.anjocaido.groupmanager.events.GMUserEvent;
import org.anjocaido.groupmanager.events.GMUserEvent.Action;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public class GroupManagerListener implements Listener {
	
	EnjinMinecraftPlugin plugin;
	
	public GroupManagerListener(EnjinMinecraftPlugin plugin) {
		this.plugin = plugin;
	}
	
	@EventHandler(priority = EventPriority.MONITOR)
	public void userGroupChangeListener(GMUserEvent event) {
		Action action = event.getAction();
		if(action == Action.USER_ADDED || action == Action.USER_GROUP_CHANGED || action == Action.USER_SUBGROUP_CHANGED ||
				action == Action.USER_REMOVED) {
			Player player = event.getUser().getBukkitPlayer();
			if(player != null) {
				plugin.debug(event.getUserName() + " just got a rank change... processing...");
				plugin.listener.updatePlayerRanks(player);
			}
		}
	}

}
