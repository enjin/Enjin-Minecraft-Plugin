package com.enjin.bukkit.tasks;

import com.enjin.bukkit.listeners.ConnectionListener;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import java.util.UUID;

public class DelayedPlayerPermsUpdate implements Runnable {
    private String player;
    private String uuid;

    public DelayedPlayerPermsUpdate(String player, String uuid) {
        this.player = player;
        this.uuid = uuid;
    }

    public DelayedPlayerPermsUpdate(OfflinePlayer player) {
        this.player = player.getName();
        this.uuid = player.getUniqueId().toString();
    }

    @Override
    public void run() {
        OfflinePlayer p = uuid != null ? Bukkit.getOfflinePlayer(UUID.fromString(uuid)) : Bukkit.getOfflinePlayer(player);
        ConnectionListener.updatePlayerRanks(p);
    }
}
