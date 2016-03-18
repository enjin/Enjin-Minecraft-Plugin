package com.enjin.sponge.tasks;

import com.enjin.sponge.listeners.ConnectionListener;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;

import java.util.UUID;

public class DelayedPlayerPermsUpdate implements Runnable {
    private String player;
    private UUID uuid;

    public DelayedPlayerPermsUpdate (String player, UUID uuid) {
        this.player = player;
        this.uuid = uuid;
    }

    public DelayedPlayerPermsUpdate (Player player) {
        this.player = player.getName();
        this.uuid = player.getUniqueId();
    }

    @Override
    public void run() {
		Player p = uuid != null ? Sponge.getServer().getPlayer(uuid).get() : Sponge.getServer().getPlayer(player).get();
        ConnectionListener.updatePlayerRanks(p);
    }
}
