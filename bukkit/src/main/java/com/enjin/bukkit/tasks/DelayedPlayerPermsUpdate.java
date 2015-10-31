package com.enjin.bukkit.tasks;

import com.enjin.bukkit.listeners.ConnectionListener;

public class DelayedPlayerPermsUpdate implements Runnable {
    private ConnectionListener listener;
    private String player;
    private String uuid;

    public DelayedPlayerPermsUpdate(ConnectionListener listener, String player, String uuid) {
        this.player = player;
        this.listener = listener;
        this.uuid = uuid;
    }

    @Override
    public void run() {
        listener.updatePlayerRanks(player, uuid);
    }
}
