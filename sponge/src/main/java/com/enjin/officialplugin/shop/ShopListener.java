package com.enjin.officialplugin.shop;

import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.network.ClientConnectionEvent;

public class ShopListener {
    @Listener
    public void playerQuit(ClientConnectionEvent.Disconnect event) {
        PlayerShopInstance.getInstances().remove(event.getTargetEntity().getUniqueId());
    }
}
