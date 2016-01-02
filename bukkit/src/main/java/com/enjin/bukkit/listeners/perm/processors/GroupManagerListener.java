package com.enjin.bukkit.listeners.perm.processors;

import com.enjin.bukkit.listeners.perm.PermissionListener;
import com.enjin.bukkit.managers.VaultManager;
import net.milkbowl.vault.permission.Permission;
import org.anjocaido.groupmanager.events.GMUserEvent;
import org.anjocaido.groupmanager.events.GMUserEvent.Action;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;

public class GroupManagerListener extends PermissionListener {
    @EventHandler(priority = EventPriority.MONITOR)
    public void userGroupChangeListener(GMUserEvent event) {
        Action action = event.getAction();
        if (action == Action.USER_ADDED || action == Action.USER_GROUP_CHANGED || action == Action.USER_SUBGROUP_CHANGED || action == Action.USER_REMOVED) {
            String player = event.getUser().getName();
            OfflinePlayer op = Bukkit.getOfflinePlayer(player);
            update(op);
        }
    }

    @Override
    public void processCommand(CommandSender sender, String command, Event event) {
        String[] parts = command.split(" ");
        if (parts.length >= 3) {
            if (parts[0].equalsIgnoreCase("manudel") || parts[0].equalsIgnoreCase("manudelsub")) {
                if (parts[1].length() > 16 || !VaultManager.isVaultEnabled()) {
                    return;
                }

                OfflinePlayer op = Bukkit.getOfflinePlayer(parts[1]);
                if (op == null) {
                    return;
                }

                Permission permission = VaultManager.getPermission();
                if (permission != null && groupExists(permission, parts[2])) {
                    update(op);
                }
            }
        }
    }

    private boolean groupExists(Permission permission, String group) {
        String[] groups = permission.getGroups();
        for (String g : groups) {
            if (group.equalsIgnoreCase(g)) {
                return true;
            }
        }
        return false;
    }
}
