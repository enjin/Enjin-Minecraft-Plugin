package com.enjin.officialplugin.shop;

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

    public PlayerShopInstance(List<Shop> shops) {
        update(shops);
    }

    public void update(List<Shop> shops) {
        if (!this.shops.isEmpty()) {
            this.shops.clear();
        }

        this.shops.addAll(shops);
        updateShop(-1);
    }

    public void updateShop(int index) {
        if (index < 0) {
            if (shops.size() != 1) {
                activeShop = null;
            } else {
                activeShop = shops.get(0);
            }
        } else {
            if (index < shops.size()) {
                activeShop = shops.get(index);
            } else {
                activeShop = null;
            }
        }

        updateCategory(-1);
    }

    public void updateCategory(int index) {
        if (index < 0) {
            if (activeShop != null) {
                if (activeShop.getCategories().size() != 1) {
                    activeCategory = null;
                } else {
                    activeCategory = activeShop.getCategories().get(0);
                }
            }
        } else {
            if (index < activeShop.getCategories().size()) {
                activeCategory = activeShop.getCategories().get(index);
            } else {
                activeCategory = null;
            }
        }
    }
}
