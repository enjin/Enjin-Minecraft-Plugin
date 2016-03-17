package com.enjin.sponge.tasks;

import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;

public class DelayedPlayerPermsUpdate implements Runnable {
    private String player;
    private String uuid;

    public DelayedPlayerPermsUpdate (String player, String uuid) {
        this.player = player;
        this.uuid = uuid;
    }

    public DelayedPlayerPermsUpdate (Player player) {
        this.player = player.getName();
        this.uuid = player.getUniqueId().toString();
    }

    @Override
    public void run() {
        Player p = uuid != null ? Sponge.getServer().getPlayer(uuid).get() : Sponge.getServer().getPlayer(player).get();
        // TODO: ConnectionListener.updatePlayerRanks(p);
    }
}
