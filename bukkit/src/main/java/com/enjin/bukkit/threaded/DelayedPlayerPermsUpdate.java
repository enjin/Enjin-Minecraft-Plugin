package com.enjin.bukkit.threaded;

import com.enjin.bukkit.EnjinMinecraftPlugin;
import org.bukkit.entity.Player;

import com.enjin.bukkit.EMPListener;

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
