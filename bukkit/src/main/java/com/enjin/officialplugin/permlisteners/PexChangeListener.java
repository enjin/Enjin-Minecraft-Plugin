package com.enjin.officialplugin.permlisteners;

import com.enjin.core.Enjin;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import com.enjin.officialplugin.EnjinMinecraftPlugin;
import com.enjin.officialplugin.threaded.DelayedPlayerPermsUpdate;

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
        if (theaction == Action.DEFAULTGROUP_CHANGED || theaction == Action.RANK_CHANGED) {
            PermissionEntity theentity = event.getEntity();
            if (theentity instanceof PermissionUser) {
                PermissionUser permuser = (PermissionUser) theentity;
                Player p = Bukkit.getPlayerExact(permuser.getName());
                if (p == null) {
                    return;
                }
                Enjin.getPlugin().debug(p.getName() + " just got a rank change... processing...");
                plugin.listener.updatePlayerRanks(p);
            }
        }

    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void preCommandListener(PlayerCommandPreprocessEvent event) {
        if (event.isCancelled()) {
            return;
        }
        Player p = event.getPlayer();
        String command = event.getMessage();
        //Make sure the user has permissions to run the command, otherwise we are just wasting time...
        if (command.toLowerCase().startsWith("/pex group ")) {
            String[] args = command.split(" ");
            if (args.length > 5 && p.hasPermission("permissions.manage.membership." + args[2])) {
                if (args[3].equalsIgnoreCase("user") && (args[4].equalsIgnoreCase("add") || args[4].equalsIgnoreCase("remove") || args[4].equalsIgnoreCase("set"))) {
                    //This command accepts csv lists of players
                    if (args[5].contains(",")) {
                        String[] players = args[5].split(",");
                        for (int i = 0; i < players.length; i++) {
                            String ep = players[i];
                            String uuid = "";
                            if (EnjinMinecraftPlugin.supportsUUID()) {
                                OfflinePlayer op = Bukkit.getOfflinePlayer(ep);
                                uuid = op.getUniqueId().toString();
                            }
                            //We need to make sure the command executes before we actually grab the data.
                            Enjin.getPlugin().debug(ep + " just got a rank change... processing...");
                            Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new DelayedPlayerPermsUpdate(plugin.listener, ep, uuid), 2);
                        }
                    } else {
                        String ep = args[5];
                        String uuid = "";
                        if (EnjinMinecraftPlugin.supportsUUID()) {
                            OfflinePlayer op = Bukkit.getOfflinePlayer(ep);
                            uuid = op.getUniqueId().toString();
                        }
                        //We need to make sure the command executes before we actually grab the data.
                        Enjin.getPlugin().debug(ep + " just got a rank change... processing...");
                        Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new DelayedPlayerPermsUpdate(plugin.listener, ep, uuid), 2);
                    }
                }
            }
        } else if (command.toLowerCase().startsWith("/pex user ")) {
            String[] args = command.split(" ");
            if (args.length > 5 && p.hasPermission("permissions.manage.membership." + args[5])) {
                if (args[3].equalsIgnoreCase("group") && (args[4].equalsIgnoreCase("add") || args[4].equalsIgnoreCase("remove") || args[4].equalsIgnoreCase("set"))) {
                    String ep = args[2];
                    String uuid = "";
                    if (EnjinMinecraftPlugin.supportsUUID()) {
                        OfflinePlayer op = Bukkit.getOfflinePlayer(ep);
                        uuid = op.getUniqueId().toString();
                    }
                    //We need to make sure the command executes before we actually grab the data.
                    Enjin.getPlugin().debug(ep + " just got a rank change... processing...");
                    Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new DelayedPlayerPermsUpdate(plugin.listener, ep, uuid), 2);
                }
            }
        }
    }

}
