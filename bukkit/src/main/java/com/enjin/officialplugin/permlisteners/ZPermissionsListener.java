package com.enjin.officialplugin.permlisteners;

import com.enjin.officialplugin.EnjinMinecraftPlugin;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.tyrannyofheaven.bukkit.zPermissions.ZPermissionsRankChangeEvent;

public class ZPermissionsListener implements Listener {
    private EnjinMinecraftPlugin plugin;

    public ZPermissionsListener(EnjinMinecraftPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onRankChange(ZPermissionsRankChangeEvent event) {
        Player player = Bukkit.getPlayer(event.getPlayerName());

        if (player != null) {
            plugin.listener.updatePlayerRanks(player);
        } else {
            OfflinePlayer offline = Bukkit.getOfflinePlayer(event.getPlayerName());
            if (offline == null) {
                return;
            } else {
                plugin.listener.updatePlayerRanks(offline.getName(), offline.getUniqueId().toString());
            }
        }

        EnjinMinecraftPlugin.debug(event.getPlayerName() + " just got a rank change... processing...");
    }
}