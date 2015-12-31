package com.enjin.bukkit.listeners.perm;

import com.enjin.bukkit.EnjinMinecraftPlugin;
import com.enjin.bukkit.tasks.DelayedPlayerPermsUpdate;
import com.enjin.core.Enjin;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.event.Event;

public abstract class PermissionProcessor {
    public abstract void processCommand(CommandSender sender, String command, Event event);

    public void update(OfflinePlayer player) {
        if (player == null) {
            return;
        }

        Enjin.getLogger().debug(player.getName() + " just got a rank change... processing...");
        Bukkit.getScheduler().scheduleSyncDelayedTask(EnjinMinecraftPlugin.getInstance(), new DelayedPlayerPermsUpdate(player), 2);
    }
}
