package com.enjin.officialplugin;

import net.canarymod.api.entity.living.humanoid.Player;
import net.canarymod.chat.Colors;
import net.canarymod.hook.HookHandler;
import net.canarymod.hook.command.ConsoleCommandHook;
import net.canarymod.hook.command.PlayerCommandHook;
import net.canarymod.hook.player.BanHook;
import net.canarymod.hook.player.ConnectionHook;
import net.canarymod.hook.player.DisconnectionHook;
import net.canarymod.plugin.PluginListener;
import net.canarymod.plugin.Priority;

/**
 * 
 * @author OverCaste (Enjin LTE PTD).
 * This software is released under an Open Source license.
 * @copyright Enjin 2012.
 * 
 */

public class EMPListener implements PluginListener {
	
	EnjinMinecraftPlugin plugin;
	
	public EMPListener(EnjinMinecraftPlugin plugin) {
		this.plugin = plugin;
	}
	
	@HookHandler(priority = Priority.LOW)
	public void onPlayerJoin(ConnectionHook e) {
		Player p = e.getPlayer();
		updatePlayerRanks(p);
		if(!plugin.newversion.equals("") && p.hasPermission("enjin.notify.update")) {
			p.sendMessage("Enjin Minecraft plugin was updated to version " + plugin.newversion + ". Please restart your server.");
		}
		if(plugin.updatefailed && p.hasPermission("enjin.notify.failedupdate")) {
			p.sendMessage(Colors.RED + "Enjin Minecraft plugin failed to update to the newest version. Please download it manually.");
		}
		if(plugin.authkeyinvalid && p.hasPermission("enjin.notify.invalidauthkey")) {
			p.sendMessage(Colors.RED + "[Enjin Minecraft Plugin] Auth key is invalid. Please generate a new one.");
		}
		if(plugin.unabletocontactenjin && p.hasPermission("enjin.notify.connectionstatus")) {
			p.sendMessage(Colors.RED + "[Enjin Minecraft Plugin] Unable to connect to enjin, please check your settings.");
			p.sendMessage(Colors.RED + "If this problem persists please send enjin the results of the /enjin log");
		}
		if(plugin.permissionsnotworking && p.hasPermission("enjin.notify.permissionsnotworking")) {
			p.sendMessage(Colors.RED + "[Enjin Minecraft Plugin] Your permissions plugin is not configured correctly. Groups and permissions will not update. Check your server.log for more details.");
		}
	}

	@HookHandler(priority = Priority.PASSIVE)
	public void onPlayerQuit(DisconnectionHook e) {
		Player p = e.getPlayer();
		updatePlayerRanks(p);
	}

	@HookHandler(priority = Priority.PASSIVE)
	public void onPlayerBan(BanHook event) {
		if(event.isIpBan()) {
			return;
		}
		if(!plugin.banlistertask.playerIsBanned(event.getBannedPlayer().getName())) {
			Player banner = event.getModerator();
			if(banner != null) {
				plugin.bannedplayers.put(event.getBannedPlayer().getName(), banner.getName());
			}else {
				plugin.bannedplayers.put(event.getBannedPlayer().getName(), "");
			}
		}
	}

	@HookHandler(priority = Priority.PASSIVE)
	public void banAndPardonListener(PlayerCommandHook event) {
		if(event.isCanceled()) {
			return;
		}
		if(event.getCommand()[0].equalsIgnoreCase("/unban") && event.getPlayer().hasPermission("canary.command.super.ban")) {
			String[] args = event.getCommand();
			if(args.length > 1) {
				plugin.banlistertask.pardonBannedPlayer(args[1].toLowerCase());
				plugin.pardonedplayers.put(args[1].toLowerCase(), event.getPlayer().getName());
			}
		}
	}

	@HookHandler(priority = Priority.PASSIVE)
	public void consolePardonListener(ConsoleCommandHook event) {
		if(event.isCanceled()) {
			return;
		}
		if(event.getCommand()[0].equalsIgnoreCase("/unban")) {
			String[] args = event.getCommand();
			if(args.length > 1) {
				plugin.banlistertask.pardonBannedPlayer(args[1].toLowerCase());
				plugin.pardonedplayers.put(args[1].toLowerCase(), "");
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
