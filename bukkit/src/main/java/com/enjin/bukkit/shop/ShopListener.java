package com.enjin.bukkit.shop;

import com.enjin.common.shop.PlayerShopInstance;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class ShopListener implements Listener {
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        PlayerShopInstance.getInstances().remove(event.getPlayer().getUniqueId());
    }
}
