package com.enjin.officialplugin;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

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
	Map<Player, String[]> initialRankMap = new HashMap<Player, String[]>();
	
	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent e) {
		Player p = e.getPlayer();
		initialRankMap.put(p, EnjinMinecraftPlugin.permission.getPlayerGroups(p));
		//TODO: Send ranks to Ejin.
	}
	
	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent e) {
		Player p = e.getPlayer();
		String[] initialRankArray = initialRankMap.get(p);
		if(initialRankArray == null || initialRankArray.length == 0) { //no initial ranks
			String[] currentRankArray = EnjinMinecraftPlugin.permission.getPlayerGroups(p);
			if(EnjinMinecraftPlugin.usingGroupManager) {
				if(currentRankArray != null) { //no initial ranks, some ranks
					for(String rank : currentRankArray) {
						if(rank.startsWith("g:")) continue;
						EnjinMinecraftPlugin.sendAddRank(p.getWorld().getName(), rank, p.getName());
					}
				}
			} else {
				if(currentRankArray != null) { //no initial ranks, some ranks
					for(String rank : currentRankArray) {
						EnjinMinecraftPlugin.sendAddRank(p.getWorld().getName(), rank, p.getName());
					}
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
				if(EnjinMinecraftPlugin.usingGroupManager) {
					for(String s : currentRankArray) {
						if(s.startsWith("g:")) continue;
						if(!initialRanks.contains(s)) {
							EnjinMinecraftPlugin.sendAddRank(p.getWorld().getName(), s, p.getName());
						}
						currentRanks.add(s);
					}
				} else {
					for(String s : currentRankArray) {
						if(!initialRanks.contains(s)) {
							EnjinMinecraftPlugin.sendAddRank(p.getWorld().getName(), s, p.getName());
						}
						currentRanks.add(s);
					}
				}
				for(String s : initialRankArray) {
					if(!currentRanks.contains(s)) {
						EnjinMinecraftPlugin.sendRemoveRank(p.getWorld().getName(), s, p.getName());
					}
				}
				
			}
		}
		initialRankMap.remove(p);
	}
}
