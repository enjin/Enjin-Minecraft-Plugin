package com.enjin.officialplugin;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
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
	
	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent e) {
		Player p = e.getPlayer();
		//initialRankMap.put(p, EnjinMinecraftPlugin.permission.getPlayerGroups(p));
		plugin.playerperms.put(new PlayerPerms(p.getName(), p.getWorld().getName()), EnjinMinecraftPlugin.permission.getPlayerGroups(p));
		if(p.isOp() && !plugin.newversion.equals("")) {
			p.sendMessage("Enjin Minecraft plugin was updated to version " + plugin.newversion + ". Please restart your server.");
		}
	}
	
	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent e) {
		Player p = e.getPlayer();
		plugin.playerperms.put(new PlayerPerms(p.getName(), p.getWorld().getName()), EnjinMinecraftPlugin.permission.getPlayerGroups(p));
		/*
		String[] initialRankArray = initialRankMap.get(p);
		if(initialRankArray == null || initialRankArray.length == 0) { //no initial ranks
			String[] currentRankArray = EnjinMinecraftPlugin.permission.getPlayerGroups(p);
			if(currentRankArray != null) { //no initial ranks, some ranks
				for(String rank : currentRankArray) {
					if(EnjinMinecraftPlugin.usingGroupManager && rank.startsWith("g:")) continue;
					EnjinMinecraftPlugin.sendAddRank(p.getWorld().getName(), rank, p.getName());
				}
			}
		
		} else {
			String[] currentRankArray = EnjinMinecraftPlugin.permission.getPlayerGroups(p);
			if(currentRankArray == null || currentRankArray.length == 0) { //no current ranks
				for(String rank : initialRankArray) {
					EnjinMinecraftPlugin.sendRemoveRank(p.getWorld().getName(), rank, p.getName());
				}
			} else {
				HashSet<String> initialRanks = new HashSet<String>(initialRankArray.length/2);
				for(String s : initialRankArray) {
					initialRanks.add(s);
				}
				HashSet<String> currentRanks = new HashSet<String>(currentRankArray.length/2);
				for(String s : currentRankArray) {
					if(EnjinMinecraftPlugin.usingGroupManager && s.startsWith("g:")) continue;
					if(!initialRanks.contains(s)) {
						EnjinMinecraftPlugin.sendAddRank(p.getWorld().getName(), s, p.getName());
					}
					currentRanks.add(s);
				}
				for(String s : initialRankArray) {
					if(!currentRanks.contains(s)) {
						EnjinMinecraftPlugin.sendRemoveRank(p.getWorld().getName(), s, p.getName());
					}
				}
				
			}
		}
		initialRankMap.remove(p);
		*/
	}
	
	public void updatePlayerRanks(Player p) {
		plugin.playerperms.put(new PlayerPerms(p.getName(), p.getWorld().getName()), EnjinMinecraftPlugin.permission.getPlayerGroups(p));
		/*
		String[] initialRankArray = initialRankMap.get(p);
		if(initialRankArray == null || initialRankArray.length == 0) { //no initial ranks
			String[] currentRankArray = EnjinMinecraftPlugin.permission.getPlayerGroups(p);
			if(currentRankArray != null) { //no initial ranks, some ranks
				for(String rank : currentRankArray) {
					if(EnjinMinecraftPlugin.usingGroupManager && rank.startsWith("g:")) continue;
					EnjinMinecraftPlugin.sendAddRank(p.getWorld().getName(), rank, p.getName());
				}
			}
		
		} else {
			String[] currentRankArray = EnjinMinecraftPlugin.permission.getPlayerGroups(p);
			if(currentRankArray == null || currentRankArray.length == 0) { //no current ranks
				for(String rank : initialRankArray) {
					EnjinMinecraftPlugin.sendRemoveRank(p.getWorld().getName(), rank, p.getName());
				}
			} else {
				HashSet<String> initialRanks = new HashSet<String>(initialRankArray.length/2);
				for(String s : initialRankArray) {
					initialRanks.add(s);
				}
				HashSet<String> currentRanks = new HashSet<String>(currentRankArray.length/2);
				for(String s : currentRankArray) {
					if(EnjinMinecraftPlugin.usingGroupManager && s.startsWith("g:")) continue;
					if(!initialRanks.contains(s)) {
						EnjinMinecraftPlugin.sendAddRank(p.getWorld().getName(), s, p.getName());
					}
					currentRanks.add(s);
				}
				for(String s : initialRankArray) {
					if(!currentRanks.contains(s)) {
						EnjinMinecraftPlugin.sendRemoveRank(p.getWorld().getName(), s, p.getName());
					}
				}
				
			}
		}
		initialRankMap.put(p, EnjinMinecraftPlugin.permission.getPlayerGroups(p));
		*/
	}
}
