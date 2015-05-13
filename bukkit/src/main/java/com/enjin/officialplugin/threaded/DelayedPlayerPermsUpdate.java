package com.enjin.officialplugin.threaded;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import com.enjin.officialplugin.EMPListener;
import com.enjin.officialplugin.EnjinMinecraftPlugin;

public class DelayedPlayerPermsUpdate implements Runnable {

    EMPListener listener;
    String player;
    String uuid;

    @Deprecated
    public DelayedPlayerPermsUpdate(EMPListener listener, Player player) {
        this.player = player.getName();
        uuid = "";
        if (EnjinMinecraftPlugin.supportsUUID()) {
            uuid = player.getUniqueId().toString();
        }
        this.listener = listener;
    }

    public DelayedPlayerPermsUpdate(EMPListener listener, String player, String uuid) {
        this.player = player;
        this.listener = listener;
        this.uuid = uuid;
    }

    @Override
    public void run() {
        listener.updatePlayerRanks(player, uuid);
    }

}
