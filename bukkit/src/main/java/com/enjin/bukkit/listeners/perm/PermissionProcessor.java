package com.enjin.bukkit.listeners.perm;

import com.enjin.bukkit.EnjinMinecraftPlugin;
import com.enjin.bukkit.tasks.DelayedPlayerPermsUpdate;
import com.enjin.bukkit.util.Log;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.event.Cancellable;

public interface PermissionProcessor {
    public void processCommand(CommandSender sender, String command, Cancellable event);

    public default void update(OfflinePlayer player) {
        if (player == null) {
            return;
        }

        Log.debug(player.getName() + " just got a rank change... processing...");
        Bukkit.getScheduler().scheduleSyncDelayedTask(EnjinMinecraftPlugin.getInstance(), new DelayedPlayerPermsUpdate(player), 2);
    }
}
