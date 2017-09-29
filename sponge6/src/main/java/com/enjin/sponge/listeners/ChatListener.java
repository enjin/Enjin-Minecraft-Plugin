package com.enjin.sponge.listeners;

import com.enjin.sponge.managers.StatsManager;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.message.MessageChannelEvent.Chat;

public class ChatListener {
    @Listener(order = Order.LAST)
    public void playerChatEvent(Chat event) {
        if (event.isCancelled()) {
            return;
        }

        StatsManager.getPlayerStats(event.getCause().first(Player.class).get()).addChatLine();
    }
}
