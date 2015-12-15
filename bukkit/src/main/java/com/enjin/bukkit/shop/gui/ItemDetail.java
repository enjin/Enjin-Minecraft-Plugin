package com.enjin.bukkit.shop.gui;

import com.enjin.bukkit.EnjinMinecraftPlugin;
import com.enjin.bukkit.managers.PurchaseManager;
import com.enjin.bukkit.shop.ShopListener;
import com.enjin.bukkit.shop.TextShopUtil;
import com.enjin.bukkit.util.text.TextUtils;
import com.enjin.bukkit.util.ui.Menu;
import com.enjin.bukkit.util.ui.MenuItem;
import com.enjin.rpc.mappings.mappings.shop.Category;
import com.enjin.rpc.mappings.mappings.shop.Item;
import com.enjin.rpc.mappings.mappings.shop.Shop;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.material.MaterialData;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ItemDetail extends Menu {
    private static DecimalFormat priceFormat = new DecimalFormat("#.00");

    public ItemDetail(Menu parent, Shop shop, Item item) {
        super(ChatColor.GOLD + item.getName().substring(0, item.getName().length() >= 30 ? 30 : item.getName().length()), 6);

        init(parent, shop, item);
    }

    private void init(Menu parent, Shop shop, Item item) {
        MenuItem back = new MenuItem(ChatColor.translateAlternateColorCodes('&', "&" + shop.getColorText()) + "Back", new MaterialData(Material.ARROW)) {
            @Override
            public void onClick(Player player) {
                if (parent != null) {
                    switchMenu(EnjinMinecraftPlugin.getInstance().getMenuAPI(), player, parent);
                    ShopListener.getGuiInstances().put(player.getUniqueId(), parent);
                }
            }
        };
        addMenuItem(back, 0);

        Material material = Material.PAPER;

        for (Material mat : Material.values()) {
            if (mat.name().toLowerCase().equalsIgnoreCase(item.getIconItem())) {
                material = mat;
            }
        }

        String name = ChatColor.translateAlternateColorCodes('&', "&" + shop.getColorName()) + item.getName();
        MenuItem menuItem = new MenuItem(name.substring(0, name.length() >= 32 ? 32 : name.length()), new MaterialData(material, (byte) item.getIconDamage())) {
            @Override
            public void onClick(Player player) {}
        };
        menuItem.setDescriptions(TextUtils.splitToListWithPrefix(item.getInfo(), 30, ChatColor.translateAlternateColorCodes('&', "&" + shop.getColorInfo())));
        addMenuItem(menuItem, 4);

        MenuItem pointOption = null;
        if (item.getPoints() != null) {
            pointOption = new MenuItem(ChatColor.translateAlternateColorCodes('&', "&" + shop.getColorText()) + "Buy with Points", new MaterialData(Material.EMERALD)) {
                @Override
                public void onClick(Player player) {
                    closeMenu(player);
                    PurchaseManager.processItemPurchase(player, item);
                }
            };
            pointOption.setDescriptions(new ArrayList<String>(){{
                add(ChatColor.translateAlternateColorCodes('&', "&" + shop.getColorText()) + "POINTS: " + ChatColor.translateAlternateColorCodes('&', "&" + shop.getColorPrice()) + (item.getPoints() == 0 ? "FREE" : item.getPoints()));
            }});
        }

        MenuItem priceOption = null;
        if (item.getPrice() != null) {
            priceOption = new MenuItem(ChatColor.translateAlternateColorCodes('&', "&" + shop.getColorText()) + "Buy with Money", new MaterialData(Material.DIAMOND)) {
                @Override
                public void onClick(Player player) {
                    closeMenu(player);
                    TextShopUtil.sendItemInfo(player, shop, item);
                }
            };
            priceOption.setDescriptions(new ArrayList<String>(){{
                add(ChatColor.translateAlternateColorCodes('&', "&" + shop.getColorText()) + "PRICE: " + ChatColor.translateAlternateColorCodes('&', "&" + shop.getColorPrice()) + (item.getPrice() == 0.0 ? "FREE" : priceFormat.format(item.getPrice()) + " " + shop.getCurrency()));
            }});
        }

        if (pointOption != null && priceOption != null) {
            addMenuItem(pointOption, 7);
            addMenuItem(priceOption, 8);
        } else {
            addMenuItem(pointOption != null ? pointOption : priceOption, 8);
        }
    }
}
