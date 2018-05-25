package com.enjin.bukkit.shop.gui;

import com.enjin.bukkit.EnjinMinecraftPlugin;
import com.enjin.bukkit.shop.ShopListener;
import com.enjin.bukkit.util.text.TextUtils;
import com.enjin.bukkit.util.ui.Menu;
import com.enjin.bukkit.util.ui.MenuItem;
import com.enjin.common.shop.PlayerShopInstance;
import com.enjin.rpc.mappings.mappings.shop.Shop;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.material.MaterialData;

import java.util.HashMap;
import java.util.Map;

public class ShopList extends Menu {
    private final Map<MenuItem, Menu> shops = new HashMap<>();

    public ShopList(Player player) {
        super(ChatColor.GOLD + "Select A Shop", 6);

        if (!PlayerShopInstance.getInstances().containsKey(player.getUniqueId())) {
            return;
        }

        init(PlayerShopInstance.getInstances().get(player.getUniqueId()));
        ShopListener.getGuiInstances().put(player.getUniqueId(), this);
    }

    private void init(PlayerShopInstance instance) {
        int i = 0;
        for (final Shop shop : instance.getShops()) {
            String name = ChatColor.translateAlternateColorCodes('&', "&" + shop.getColorId()) + (i + 1) + ". " + ChatColor.translateAlternateColorCodes('&', "&" + shop.getColorName()) + shop.getName();
            MenuItem menuItem = new MenuItem(name.substring(0, name.length() >= 32 ? 32 : name.length()), new MaterialData(Material.CHEST)) {
                @Override
                public void onClick(Player player) {
                    if (!shops.containsKey(this)) {
                        shops.put(this, new CategoryList(ShopList.this, shop));
                    }

                    switchMenu(EnjinMinecraftPlugin.getInstance().getMenuAPI(), player, shops.get(this));
                    ShopListener.getGuiInstances().put(player.getUniqueId(), shops.get(this));
                }
            };
            menuItem.setDescriptions(TextUtils.splitToListWithPrefix(shop.getInfo(), 30, ChatColor.translateAlternateColorCodes('&', "&" + shop.getColorInfo())));

            this.addMenuItem(menuItem, i++);
        }
    }
}
