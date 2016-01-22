package com.enjin.bukkit.listeners.perm.processors;

import com.enjin.bukkit.listeners.perm.PermissionListener;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;

import ru.tehkode.permissions.PermissionEntity;
import ru.tehkode.permissions.PermissionUser;
import ru.tehkode.permissions.events.PermissionEntityEvent;
import ru.tehkode.permissions.events.PermissionEntityEvent.Action;

public class PexListener extends PermissionListener {
    @EventHandler(priority = EventPriority.MONITOR)
    public void pexGroupAdded(PermissionEntityEvent event) {
        Action theaction = event.getAction();
        if (theaction == Action.DEFAULTGROUP_CHANGED || theaction == Action.RANK_CHANGED) {
            PermissionEntity theentity = event.getEntity();
            if (theentity instanceof PermissionUser) {
                PermissionUser permuser = (PermissionUser) theentity;
                OfflinePlayer op = Bukkit.getOfflinePlayer(permuser.getName());
                update(op);
            }
        }

    }

    public void processCommand(CommandSender sender, String command, Event event) {
        //Make sure the user has permissions to run the value, otherwise we are just wasting time...
        if (command.toLowerCase().startsWith("pex group ")) {
            String[] args = command.split(" ");
            if (args.length > 5 && sender.hasPermission("permissions.manage.membership." + args[2])) {
                if (args[3].equalsIgnoreCase("user") && (args[4].equalsIgnoreCase("add") || args[4].equalsIgnoreCase("remove") || args[4].equalsIgnoreCase("set"))) {
                    //This value accepts csv lists of players
                    if (args[5].contains(",")) {
                        String[] players = args[5].split(",");
                        for (int i = 0; i < players.length; i++) {
                            String ep = players[i];
                            OfflinePlayer op = Bukkit.getOfflinePlayer(ep);
                            //We need to make sure the value executes before we actually grab the data.
                            update(op);
                        }
                    } else {
                        String ep = args[5];
                        OfflinePlayer op = Bukkit.getOfflinePlayer(ep);
                        //We need to make sure the value executes before we actually grab the data.
                        update(op);
                    }
                }
            }
        } else if (command.toLowerCase().startsWith("pex user ")) {
            String[] args = command.split(" ");
            if (args.length > 5 && sender.hasPermission("permissions.manage.membership." + args[5])) {
                if (args[3].equalsIgnoreCase("group") && (args[4].equalsIgnoreCase("add") || args[4].equalsIgnoreCase("remove") || args[4].equalsIgnoreCase("set"))) {
                    String ep = args[2];
                    OfflinePlayer op = Bukkit.getOfflinePlayer(ep);
                    //We need to make sure the value executes before we actually grab the data.
                    update(op);
                }
            }
        }
    }
}
