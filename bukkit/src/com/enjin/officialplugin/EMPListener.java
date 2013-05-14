package com.enjin.officialplugin;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
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
	
	public EMPListener(EnjinMinecraftPlugin plugin) {
		this.plugin = plugin;
	}
	
	@EventHandler(priority = EventPriority.MONITOR)
	public void onPlayerJoin(PlayerJoinEvent e) {
		Player p = e.getPlayer();
		updatePlayerRanks(p);
		if(!plugin.newversion.equals("") && p.hasPermission("enjin.notify.update")) {
			p.sendMessage("Enjin Minecraft plugin was updated to version " + plugin.newversion + ". Please restart your server.");
		}
		if(plugin.updatefailed && p.hasPermission("enjin.notify.failedupdate")) {
			p.sendMessage(ChatColor.DARK_RED + "Enjin Minecraft plugin failed to update to the newest version. Please download it manually.");
		}
		if(plugin.authkeyinvalid && p.hasPermission("enjin.notify.invalidauthkey")) {
			p.sendMessage(ChatColor.DARK_RED + "[Enjin Minecraft Plugin] Auth key is invalid. Please generate a new one.");
		}
		if(plugin.unabletocontactenjin && p.hasPermission("enjin.notify.connectionstatus")) {
			p.sendMessage(ChatColor.DARK_RED + "[Enjin Minecraft Plugin] Unable to connect to enjin, please check your settings.");
			p.sendMessage(ChatColor.DARK_RED + "If this problem persists please send enjin the results of the /enjin log");
		}
		if(plugin.permissionsnotworking && p.hasPermission("enjin.notify.permissionsnotworking")) {
			p.sendMessage(ChatColor.DARK_RED + "[Enjin Minecraft Plugin] Your permissions plugin is not configured correctly. Groups and permissions will not update. Check your server.log for more details.");
		}
	}
	
	@EventHandler(priority = EventPriority.LOWEST)
	public void onPlayerQuit(PlayerQuitEvent e) {
		Player p = e.getPlayer();
		updatePlayerRanks(p);
	}
	
	@EventHandler(priority = EventPriority.LOWEST)
	public void onPlayerBan(PlayerKickEvent event) {
		if(event.isCancelled()) {
			return;
		}
		if(event.getPlayer().isBanned() && !plugin.banlistertask.playerIsBanned(event.getPlayer().getName())) {
			plugin.bannedplayers.put(event.getPlayer().getName(), "");
		}
	}
	
	@EventHandler(priority = EventPriority.LOWEST)
	public void banAndPardonListener(PlayerCommandPreprocessEvent event) {
		if(event.isCancelled()) {
			return;
		}
		if(event.getMessage().toLowerCase().startsWith("/ban ") && event.getPlayer().hasPermission("bukkit.command.ban.player")) {
			String[] args = event.getMessage().split(" ");
			if(args.length > 1) {
				plugin.banlistertask.addBannedPlayer(args[1].toLowerCase());
				plugin.bannedplayers.put(args[1].toLowerCase(), event.getPlayer().getName());
			}
		}else if(event.getMessage().toLowerCase().startsWith("/pardon ") && event.getPlayer().hasPermission("bukkit.command.unban.player")) {
			String[] args = event.getMessage().split(" ");
			if(args.length > 1) {
				plugin.banlistertask.pardonBannedPlayer(args[1].toLowerCase());
				plugin.pardonedplayers.put(args[1].toLowerCase(), event.getPlayer().getName());
			}
		}
	}
	
	public void updatePlayerRanks(Player p) {
		updatePlayerRanks(p.getName());
	}
	
	public void updatePlayerRanks(String p) {
		plugin.playerperms.put(p, "");
	}
}
