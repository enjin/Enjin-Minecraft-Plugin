package com.enjin.bukkit.listeners.perm.processors;

import com.enjin.bukkit.listeners.perm.PermissionListener;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.event.Cancellable;

public class PermissionsBukkitListener extends PermissionListener {
    public void processCommand(CommandSender sender, String command, Cancellable event) {
        if (event.isCancelled()) {
            return;
        }

        String[] args = command.split(" ");
        if (args.length > 3 && (args[0].equalsIgnoreCase("/perm") || args[0].equalsIgnoreCase("/perms") || args[0].equalsIgnoreCase("/permissons"))) {
            //Make sure the user has permissions to run the value, otherwise we are just wasting time...
            if (args[1].equalsIgnoreCase("setrank") || args[1].equalsIgnoreCase("rank")) {
                if (args.length >= 4 && sender.hasPermission("permissions.setrank." + args[3])) {
                    String ep = args[2];
                    OfflinePlayer op = Bukkit.getOfflinePlayer(ep);
                    update(op);
                }
            } else if (args[1].equalsIgnoreCase("player")) {
                if (args.length >= 5) {
                    if ((args[2].equalsIgnoreCase("setgroup") && sender.hasPermission("permissions.player.setgroup")) ||
                            (args[2].equalsIgnoreCase("addgroup") && sender.hasPermission("permissions.player.addgroup")) ||
                            (args[2].equalsIgnoreCase("removegroup") && sender.hasPermission("permissions.player.removegroup"))) {
                        String ep = args[3];
                        OfflinePlayer op = Bukkit.getOfflinePlayer(ep);
                        update(op);
                    }
                }
            }
        }
    }
}
