package com.enjin.officialplugin.listeners;

import net.canarymod.api.entity.living.monster.Creeper;
import net.canarymod.hook.HookHandler;
import net.canarymod.hook.player.BlockDestroyHook;
import net.canarymod.hook.player.BlockPlaceHook;
import net.canarymod.hook.player.ConnectionHook;
import net.canarymod.hook.player.ExperienceHook;
import net.canarymod.hook.player.KickHook;
import net.canarymod.hook.player.PlayerDeathHook;
import net.canarymod.hook.world.ExplosionHook;
import net.canarymod.plugin.PluginListener;
import net.canarymod.plugin.Priority;

import com.enjin.officialplugin.EnjinMinecraftPlugin;
import com.enjin.officialplugin.stats.StatsPlayer;

public class EnjinStatsListener implements PluginListener {

    EnjinMinecraftPlugin plugin;

    public EnjinStatsListener(EnjinMinecraftPlugin plugin) {
        this.plugin = plugin;
    }

    @HookHandler(priority = Priority.PASSIVE)
    public void onPlayerJoin(ConnectionHook e) {
        if (!plugin.playerstats.containsKey(e.getPlayer().getName().toLowerCase())) {
            plugin.playerstats.put(e.getPlayer().getName().toLowerCase(), new StatsPlayer(e.getPlayer().getName()));
        }
    }

    @HookHandler(priority = Priority.PASSIVE)
    public void onExplosion(ExplosionHook event) {
        if (event.isCanceled()) {
            return;
        }
        if (event.getEntity() != null && event.getEntity() instanceof Creeper) {
            plugin.serverstats.addCreeperExplosion();
        }
    }

    @HookHandler(priority = Priority.PASSIVE)
    public void onKick(KickHook event) {
        plugin.serverstats.addKick(event.getKickedPlayer().getName());
    }

    @HookHandler(priority = Priority.PASSIVE)
    public void onPlayerBreakBlock(BlockDestroyHook event) {
        if (event.isCanceled()) {
            return;
        }
        StatsPlayer stats = plugin.GetPlayerStats(event.getPlayer().getName());
        stats.addBrokenBlock(event.getBlock());
        //plugin.debug("Got a block broken by " + event.getPlayer().getName() + ". Bock type: " + event.getBlock().getType().toString());
    }

    @HookHandler(priority = Priority.PASSIVE)
    public void onPlayerPlaceBlock(BlockPlaceHook event) {
        if (event.isCanceled()) {
            return;
        }
        StatsPlayer stats = plugin.GetPlayerStats(event.getPlayer().getName());
        stats.addPlacedBlock(event.getBlockPlaced());
        //plugin.debug("Got a block placed by " + event.getPlayer().getName() + ". Bock type: " + event.getBlock().getType().toString());
    }

    //Listener to increment player deaths
    @HookHandler(priority = Priority.PASSIVE)
    public void onPlayerDeath(PlayerDeathHook event) {
        StatsPlayer stats = plugin.GetPlayerStats(event.getPlayer().getName());
        stats.addDeath();
    }

    //Listener to increment player kills
    //TODO: Implement this


    //TODO: get those methods implemented!
    //Listener to add distance traveled to players
    /*
    @HookHandler(priority = Priority.PASSIVE)
	public void onPlayerMove(PlayerMoveHook event) {
		if(event.isCanceled()) {
			return;
		}
		Player player = event.getPlayer();
		Location from = event.getFrom();
		Location to = event.getTo();
		//Make sure we didn't teleport between two worlds or something
		if(!from.getWorld().getName().equals(to.getWorld().getName())) {
			return;
		}
		double distance = (to.getX() - from.getX()) * (to.getX() - from.getX()) + (to.getY() - from.getY()) * (to.getY() - from.getY()) + (to.getZ() - from.getZ()) * (to.getZ() - from.getZ());
		//You can't go over 4 blocks before a teleport event happens, otherwise you are using hacks to move.
		if(distance > 36) {
			return;
		}
		StatsPlayer splayer = plugin.GetPlayerStats(player.getName());
		if(player.isInVehicle()) {
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
	}*/

    @HookHandler(priority = Priority.PASSIVE)
    public void xpChange(ExperienceHook event) {
        if (event.getNewValue() == 0) {
            return;
        }
        StatsPlayer splayer = plugin.GetPlayerStats(event.getPlayer().getName());
        splayer.setXplevel(event.getPlayer().getLevel());
        splayer.setTotalxp(plugin.getTotalXP(event.getPlayer().getLevel(), event.getPlayer().getExperience()));
    }
}
