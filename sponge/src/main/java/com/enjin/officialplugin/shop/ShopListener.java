package com.enjin.officialplugin.shop;

import org.spongepowered.api.event.Subscribe;
import org.spongepowered.api.event.entity.player.PlayerQuitEvent;

public class ShopListener {
    @Subscribe
    public void playerQuit(PlayerQuitEvent event) {
        PlayerShopInstance.getInstances().remove(event.getEntity().getUniqueId());
    }
}
