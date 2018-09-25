package com.enjin.bukkit.listeners;

import com.enjin.bukkit.EnjinMinecraftPlugin;
import com.enjin.bukkit.config.EMPConfig;
import com.enjin.bukkit.tasks.BanLister;
import com.enjin.bukkit.util.PermissionsUtil;
import com.enjin.core.Enjin;
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

        if (BanLister.getInstance() == null) {
            return;
        }

        if (event.getPlayer() == null) {
            return;
        }

        if (event.getPlayer().isBanned() && !BanLister.getInstance().playerIsBanned(event.getPlayer())) {
            BanLister.getInstance().addBannedPlayer(event.getPlayer());
            plugin.getBannedPlayers().put(event.getPlayer().getName(), "");
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerCommandPreProcess(PlayerCommandPreprocessEvent event) {
        if (!Enjin.getConfiguration(EMPConfig.class).isListenForBans()) {
            return;
        }

        if (event.isCancelled()) {
            return;
        }

        if (event.getMessage().toLowerCase().startsWith("/ban") && PermissionsUtil.hasPermission(event.getPlayer(),
                                                                                                 "bukkit.permission.ban.player")) {
            String[] args = event.getMessage().split(" ");

            if (args.length > 1) {
                OfflinePlayer player = Bukkit.getOfflinePlayer(args[1]);
                BanLister.getInstance().addBannedPlayer(player);
                plugin.getBannedPlayers().put(args[1].toLowerCase(), event.getPlayer().getName());
            }
        } else if (event.getMessage()
                        .toLowerCase()
                        .startsWith("/pardon") && PermissionsUtil.hasPermission(event.getPlayer(),
                                                                                "bukkit.permission.unban.player")) {
            String[] args = event.getMessage().split(" ");

            if (args.length > 1) {
                OfflinePlayer player = Bukkit.getOfflinePlayer(args[1]);
                BanLister.getInstance().pardonBannedPlayer(player);
                plugin.getPardonedPlayers().put(args[1].toLowerCase(), event.getPlayer().getName());
            }
        }
    }
}
