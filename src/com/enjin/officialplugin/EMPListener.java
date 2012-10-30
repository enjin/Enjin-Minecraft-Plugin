package com.enjin.officialplugin;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

/**
 * 
 * @author OverCaste (Enjin LTE PTD).
 * This software is released under an Open Source license.
 * @copyright Enjin 2012.
 * 
 */

public class EMPListener implements Listener {
	
	EnjinMinecraftPlugin plugin;
	//Map<Player, String[]> initialRankMap = new HashMap<Player, String[]>();
	
	public EMPListener(EnjinMinecraftPlugin plugin) {
		this.plugin = plugin;
	}
	
	@EventHandler(priority = EventPriority.MONITOR)
	public void onPlayerJoin(PlayerJoinEvent e) {
		Player p = e.getPlayer();
		updatePlayerRanks(p);
		if(p.isOp() && !plugin.newversion.equals("")) {
			p.sendMessage("Enjin Minecraft plugin was updated to version " + plugin.newversion + ". Please restart your server.");
		}if(p.isOp() && plugin.updatefailed) {
			p.sendMessage(ChatColor.DARK_RED + "Enjin Minecraft plugin failed to update to the newest version. Please download it manually.");
		}
	}
	
	@EventHandler(priority = EventPriority.LOWEST)
	public void onPlayerQuit(PlayerQuitEvent e) {
		Player p = e.getPlayer();
		updatePlayerRanks(p);
	}
	
	public void updatePlayerRanks(Player p) {
		//permissionsbukkit doesn't support per world permissions
		/*
		if(plugin.permissionsbukkit != null) {
			plugin.playerperms.put(new PlayerPerms(p.getName(), (String)null), EnjinMinecraftPlugin.permission.getPlayerGroups((String)null, p.getName()));
		}
		plugin.playerperms.put(new PlayerPerms(p.getName(), p.getWorld().getName()), EnjinMinecraftPlugin.permission.getPlayerGroups(p));*/
		updatePlayerRanks(p.getName());
	}
	
	public void updatePlayerRanks(String p) {
		plugin.playerperms.put(p, "");
	}
}
