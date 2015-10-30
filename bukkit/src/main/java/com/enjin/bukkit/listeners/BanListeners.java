package com.enjin.bukkit.listeners;

import com.enjin.bukkit.EnjinMinecraftPlugin;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerKickEvent;

public class BanListeners implements Listener {
    EnjinMinecraftPlugin plugin;

    public BanListeners(EnjinMinecraftPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerKick(PlayerKickEvent event) {
        if (event.isCancelled()) {
            return;
        }
        if (event.getPlayer().isBanned() && !plugin.banlistertask.playerIsBanned(event.getPlayer())) {
            plugin.banlistertask.addBannedPlayer(event.getPlayer());
            plugin.bannedplayers.put(event.getPlayer().getName(), "");
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerCommandPreProcess(PlayerCommandPreprocessEvent event) {
        if (event.isCancelled()) {
            return;
        }

        if (event.getMessage().toLowerCase().startsWith("/ban") && event.getPlayer().hasPermission("bukkit.permission.ban.player")) {
            String[] args = event.getMessage().split(" ");

            if (args.length > 1) {
                OfflinePlayer player = Bukkit.getOfflinePlayer(args[1]);
                plugin.banlistertask.addBannedPlayer(player);
                plugin.bannedplayers.put(args[1].toLowerCase(), event.getPlayer().getName());
            }
        } else if (event.getMessage().toLowerCase().startsWith("/pardon") && event.getPlayer().hasPermission("bukkit.permission.unban.player")) {
            String[] args = event.getMessage().split(" ");

            if (args.length > 1) {
                OfflinePlayer player = Bukkit.getOfflinePlayer(args[1]);
                plugin.banlistertask.pardonBannedPlayer(player);
                plugin.pardonedplayers.put(args[1].toLowerCase(), event.getPlayer().getName());
            }
        }
    }
}
