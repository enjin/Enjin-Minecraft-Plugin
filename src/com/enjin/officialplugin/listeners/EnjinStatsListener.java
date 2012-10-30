package com.enjin.officialplugin.listeners;

import org.bukkit.Location;
import org.bukkit.entity.Boat;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Minecart;
import org.bukkit.entity.Pig;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;

import com.enjin.officialplugin.EnjinMinecraftPlugin;
import com.enjin.officialplugin.stats.StatsPlayer;

public class EnjinStatsListener implements Listener {
	
	EnjinMinecraftPlugin plugin;
	
	public EnjinStatsListener(EnjinMinecraftPlugin plugin) {
		this.plugin = plugin;
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onPlayerJoin(PlayerJoinEvent e) {
		if(!plugin.playerstats.containsKey(e.getPlayer().getName().toLowerCase())) {
			plugin.playerstats.put(e.getPlayer().getName().toLowerCase(), new StatsPlayer(e.getPlayer().getName()));
		}
	}
	
	@EventHandler(priority = EventPriority.MONITOR)
	public void onPlayerBreakBlock(BlockBreakEvent event) {
		if(event.isCancelled()) {
			return;
		}
		StatsPlayer stats = GetPlayerStats(event.getPlayer().getName());
		stats.addBrokenBlock(event.getBlock());
		//plugin.debug("Got a block broken by " + event.getPlayer().getName() + ". Bock type: " + event.getBlock().getType().toString());
	}
	
	@EventHandler(priority = EventPriority.MONITOR)
	public void onPlayerPlaceBlock(BlockPlaceEvent event) {
		if(event.isCancelled()) {
			return;
		}
		StatsPlayer stats = GetPlayerStats(event.getPlayer().getName());
		stats.addPlacedBlock(event.getBlock());
		//plugin.debug("Got a block placed by " + event.getPlayer().getName() + ". Bock type: " + event.getBlock().getType().toString());
	}
	
	//Listener to increment player deaths
	@EventHandler(priority = EventPriority.MONITOR)
	public void onPlayerDeath(PlayerDeathEvent event) {
		StatsPlayer stats = GetPlayerStats(event.getEntity().getName());
		stats.addDeath();
	}
	
	//Listener to increment player kills
	@EventHandler(priority = EventPriority.MONITOR)
	public void onEntityDeath(EntityDeathEvent event) {
		if(event.getEntity().getLastDamageCause() instanceof EntityDamageByEntityEvent) {
			EntityDamageByEntityEvent damageevent = (EntityDamageByEntityEvent)event.getEntity().getLastDamageCause();
			if(damageevent.getDamager() instanceof Player) {
				StatsPlayer stats = GetPlayerStats(((Player)damageevent.getDamager()).getName());
				stats.addKilled();
				//Let's add to the player's pvp or pve kills
				if(event.getEntityType() == EntityType.PLAYER) {
					stats.addPvpkill();
				}else {
					stats.addPvekill(event.getEntityType());
				}
			}
		}
	}
	
	//Listener to add distance traveled to players
	@EventHandler(priority = EventPriority.MONITOR)
	public void onPlayerMove(PlayerMoveEvent event) {
		if(event.isCancelled()) {
			return;
		}
		Player player = event.getPlayer();
		Location from = event.getFrom();
		Location to = event.getTo();
		//Make sure we didn't teleport between two worlds or something
		if(!from.getWorld().getName().equals(to.getWorld().getName())) {
			return;
		}
		double distance = Math.sqrt((to.getX() - from.getX()) * (to.getX() - from.getX()) + (to.getY() - from.getY()) * (to.getY() - from.getY()) + (to.getZ() - from.getZ()) * (to.getZ() - from.getZ()));
		//You can't go over 4 blocks before a teleport event happens, otherwise you are using hacks to move.
		if(distance > 6) {
			return;
		}
		StatsPlayer splayer = GetPlayerStats(player.getName());
		if(player.isInsideVehicle()) {
			Entity vehicle = player.getVehicle();
			if(vehicle instanceof Minecart) {
				splayer.addMinecartdistance(distance);
			}else if(vehicle instanceof Boat) {
				splayer.addBoatdistance(distance);
			}else if(vehicle instanceof Pig) {
				splayer.addPigdistance(distance);
			}else {
				//They are riding an entity we can't identify
				//In the future we should allow custom entities
			}
		}else {
			//The player could also be flying, so we need to add that
			splayer.addFootdistance(distance);
		}
	}
	
	private StatsPlayer GetPlayerStats(String name) {
		StatsPlayer stats = plugin.playerstats.get(name.toLowerCase());
		if(stats == null) {
			stats = new StatsPlayer(name);
			plugin.playerstats.put(name.toLowerCase(), stats);
		}
		return stats;
	}
}
