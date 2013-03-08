package com.enjin.officialplugin;

import cpw.mods.fml.common.IPlayerTracker;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.event.ForgeSubscribe;

/**
 * 
 * @author OverCaste (Enjin LTE PTD).
 * This software is released under an Open Source license.
 * @copyright Enjin 2012.
 * 
 */

public class EMPListener implements IPlayerTracker {
	
	EnjinMinecraftPlugin plugin;
	
	public EMPListener(EnjinMinecraftPlugin plugin) {
		this.plugin = plugin;
	}
	
	//TODO: Add ban listeners back in
	/*
	public void onPlayerBan(PlayerKickEvent event) {
		if(event.isCancelled()) {
			return;
		}
		if(event.getPlayer().isBanned() && !plugin.banlistertask.playerIsBanned(event.getPlayer().getName())) {
			plugin.bannedplayers.put(event.getPlayer().getName(), "");
		}
	}
	
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
	}*/
	
	public void updatePlayerRanks(EntityPlayer p) {
		updatePlayerRanks(p.getEntityName());
	}
	
	public void updatePlayerRanks(String p) {
		plugin.playerperms.put(p, "");
	}

	@Override
	public void onPlayerLogin(EntityPlayer p) {
		//updatePlayerRanks(player);
		if(!MinecraftServer.getServer().getConfigurationManager().getOps().contains(p.getEntityName().toLowerCase())) {
			return;
		}
		if(!plugin.newversion.equals("")) {
			p.sendChatToPlayer("Enjin Minecraft plugin was updated to version " + plugin.newversion + ". Please restart your server.");
		}
		if(plugin.updatefailed) {
			p.sendChatToPlayer(ChatColor.DARK_RED + "Enjin Minecraft plugin failed to update to the newest version. Please download it manually.");
		}
		if(plugin.authkeyinvalid) {
			p.sendChatToPlayer(ChatColor.DARK_RED + "[Enjin Minecraft Plugin] Auth key is invalid. Please generate a new one.");
		}
		if(plugin.unabletocontactenjin) {
			p.sendChatToPlayer(ChatColor.DARK_RED + "[Enjin Minecraft Plugin] Unable to connect to enjin, please check your settings.");
			p.sendChatToPlayer(ChatColor.DARK_RED + "If this problem persists please send enjin the results of the /enjin report");
		}
	}

	@Override
	public void onPlayerLogout(EntityPlayer player) {
		updatePlayerRanks(player);
	}

	@Override
	public void onPlayerChangedDimension(EntityPlayer player) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onPlayerRespawn(EntityPlayer player) {
		// TODO Auto-generated method stub
		
	}
}
