package com.enjin.bukkit.listeners.perm.processors;

import com.enjin.bukkit.listeners.perm.PermissionListener;
import org.anjocaido.groupmanager.events.GMUserEvent;
import org.anjocaido.groupmanager.events.GMUserEvent.Action;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.event.Cancellable;
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
    public void processCommand(CommandSender sender, String command, Cancellable event) {
        // Not Used
    }
}
