package com.enjin.bukkit.listeners.perm.processors;

import com.enjin.bukkit.listeners.perm.PermissionListener;
import com.google.common.collect.Lists;
import com.platymuus.bukkit.permissions.Group;
import com.platymuus.bukkit.permissions.PermissionsPlugin;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.event.Event;

import java.util.List;

public class PermissionsBukkitListener extends PermissionListener {
    public void processCommand(CommandSender sender, String command, Event event) {
        String[] args = command.split(" ");
        if (args.length > 3 && (args[0].equalsIgnoreCase("perm") || args[0].equalsIgnoreCase("perms") || args[0].equalsIgnoreCase("permissions"))) {
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

    public static String[] getGroups(OfflinePlayer player) {
        PermissionsPlugin plugin = (PermissionsPlugin) Bukkit.getPluginManager().getPlugin("PermissionsBukkit");
        List<Group> groups = plugin.getGroups(player.getUniqueId());

        List<String> result = Lists.newArrayList();
        for (Group group : groups) {
            result.add(group.getName());
        }

        return result.toArray(new String[result.size()]);
    }
}
