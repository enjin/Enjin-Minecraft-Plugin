package com.enjin.officialplugin.shop;

import com.enjin.officialplugin.EnjinMinecraftPlugin;
import com.enjin.officialplugin.shop.data.Category;
import com.enjin.officialplugin.shop.data.Shop;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import lombok.Getter;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public class PlayerShopInstance {
    @Getter
    private static Map<UUID, PlayerShopInstance> instances = Maps.newHashMap();

    @Getter
    private List<Shop> shops = Lists.newArrayList();
    @Getter
    private Shop activeShop = null;
    @Getter
    private Category activeCategory = null;
    @Getter
    private long lastUpdated = System.currentTimeMillis();

    private EnjinMinecraftPlugin plugin;

    public PlayerShopInstance(List<Shop> shops) {
        plugin = EnjinMinecraftPlugin.getInstance();
        update(shops);
    }

    public void update(List<Shop> shops) {
        plugin.debug("Updating instance shops");
        if (!this.shops.isEmpty()) {
            plugin.debug("Clearing existing shops");
            this.shops.clear();
        }

        plugin.debug("Adding shops to instance");
        this.shops.addAll(shops);
        updateShop(-1);

        lastUpdated = System.currentTimeMillis();
    }

    public void updateShop(int index) {
        if (index < 0) {
            if (shops.size() != 1) {
                plugin.debug("There is more or less than one shop. Unsetting active shop.");
                activeShop = null;
            } else {
                activeShop = shops.get(0);
                plugin.debug("Set active shop to: " + activeShop.getName());
            }
        } else {
            if (index < shops.size()) {
                activeShop = shops.get(index);
                plugin.debug("Set active shop to: " + activeShop.getName());
            } else {
                plugin.debug("Invalid index. Unsetting active shop.");
                activeShop = null;
            }
        }

        updateCategory(-1);
    }

    public void updateCategory(int index) {
        if (index < 0) {
            activeCategory = null;
            return;
        }

        List<Category> categories = activeCategory == null ? (activeShop == null ? null : activeShop.getCategories()) : activeCategory.getCategories();

        if (categories == null || categories.isEmpty()) {
            plugin.debug("There are no categories available. Skipping category update.");
            return;
        }

        if (index < categories.size()) {
            activeCategory = categories.get(index);
        } else {
            activeCategory = categories.get(categories.size() - 1);
        }

        plugin.debug("Set active category to: " + (activeCategory == null ? "null" : activeCategory.getName()));
        if (activeCategory != null) {
            plugin.debug(activeCategory.toString());
        }
    }
}
