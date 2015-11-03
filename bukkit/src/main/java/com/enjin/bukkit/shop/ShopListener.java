package com.enjin.bukkit.shop;

import com.enjin.bukkit.util.ui.Menu;
import com.enjin.common.shop.PlayerShopInstance;
import com.google.common.collect.Maps;
import lombok.Getter;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.Map;
import java.util.UUID;

public class ShopListener implements Listener {
    @Getter
    private static final Map<UUID, Menu> guiInstances = Maps.newHashMap();

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        PlayerShopInstance.getInstances().remove(event.getPlayer().getUniqueId());
        Menu menu = guiInstances.remove(event.getPlayer().getUniqueId());

        if (menu != null) {
            menu.destroy();
        }
    }
}
