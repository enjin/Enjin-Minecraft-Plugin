package com.enjin.officialplugin;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import ru.tehkode.permissions.PermissionEntity;
import ru.tehkode.permissions.PermissionUser;
import ru.tehkode.permissions.events.PermissionEntityEvent;
import ru.tehkode.permissions.events.PermissionEntityEvent.Action;

public class PexChangeListener implements Listener {
	
	EnjinMinecraftPlugin plugin;
	
	public PexChangeListener(EnjinMinecraftPlugin plugin) {
		this.plugin = plugin;
	}
	
	@EventHandler(priority = EventPriority.MONITOR)
	public void pexGroupAdded(PermissionEntityEvent event) {
		Action theaction = event.getAction();
		if(theaction == Action.DEFAULTGROUP_CHANGED || theaction == Action.RANK_CHANGED) {
			PermissionEntity theentity = event.getEntity();
			if(theentity instanceof PermissionUser) {
				PermissionUser permuser = (PermissionUser)theentity;
				Player p = Bukkit.getPlayerExact(permuser.getName());
				if(p == null) {
					return;
				}
				plugin.debug(p.getName() + " just got a rank change... processing...");
				plugin.listener.updatePlayerRanks(p);
			}
		}
		
	}

}
