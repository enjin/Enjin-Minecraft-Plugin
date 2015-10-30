package com.enjin.bukkit.listeners.perm;

import com.enjin.bukkit.listeners.ConnectionListener;
import com.enjin.core.Enjin;
import org.anjocaido.groupmanager.events.GMUserEvent;
import org.anjocaido.groupmanager.events.GMUserEvent.Action;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import com.enjin.bukkit.EnjinMinecraftPlugin;

public class GroupManagerListener implements Listener {
    @EventHandler(priority = EventPriority.MONITOR)
    public void userGroupChangeListener(GMUserEvent event) {
        Action action = event.getAction();
        if (action == Action.USER_ADDED || action == Action.USER_GROUP_CHANGED || action == Action.USER_SUBGROUP_CHANGED ||
                action == Action.USER_REMOVED) {
            String player = event.getUser().getName();
            OfflinePlayer op = Bukkit.getOfflinePlayer(player);
            if (op != null) {
                Enjin.getPlugin().debug(event.getUserName() + " just got a rank change... processing...");
                ConnectionListener.getInstance().updatePlayerRanks(player, op.getUniqueId().toString());
            }
        }
    }
}
