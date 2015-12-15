package com.enjin.bukkit.managers;

import com.enjin.bukkit.EnjinMinecraftPlugin;
import com.enjin.bukkit.shop.TextShopUtil;
import com.enjin.bukkit.util.text.TextUtils;
import com.enjin.core.Enjin;
import com.enjin.core.EnjinServices;
import com.enjin.rpc.mappings.mappings.general.RPCData;
import com.enjin.rpc.mappings.mappings.shop.Item;
import com.enjin.rpc.mappings.mappings.shop.Shop;
import com.enjin.rpc.mappings.services.ShopService;
import lombok.Getter;
import mkremins.fanciful.FancyMessage;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class PurchaseManager {
    @Getter
    private static Map<String, Integer> pendingPurchases = new ConcurrentHashMap<>();

    public static void processItemPurchase(Player player, Shop shop, Item item) {
        if (item == null) {
            player.sendMessage(ChatColor.RED + "You must select an item from a category first.");
        } else if (item.getPoints() == null || (item.getVariables() != null && !item.getVariables().isEmpty())) {
            TextShopUtil.sendItemInfo(player, shop, item);
        } else {
            PurchaseManager.getPendingPurchases().put(player.getName(), item.getId());
            player.sendMessage(ChatColor.GREEN + "Type \"/buy confirm\" to complete your pending purchase.");
        }
    }

    public static void confirmPurchase(Player player) {
        if (!pendingPurchases.containsKey(player.getName())) {
            player.sendMessage(ChatColor.RED + "You do not have a pending purchase.");
            return;
        }

        Integer id = pendingPurchases.get(player.getName());
        pendingPurchases.remove(player.getName());

        Runnable runnable = () -> {
            ShopService service = EnjinServices.getService(ShopService.class);
            RPCData<Integer> data = service.purchase(player.getName(),
                    id,
                    Optional.empty(),
                    Optional.empty(),
                    Optional.empty(),
                    false);

            if (data == null) {
                player.sendMessage(ChatColor.RED + "A fatal error has occurred. Please try again later. If the problem persists please contact Enjin support.");
                return;
            }

            if (data.getError() != null) {
                player.sendMessage(ChatColor.RED + data.getError().getMessage());
                return;
            }

            player.sendMessage(ChatColor.GREEN + "You successfully purchased the item. Your new point balance is: " + data.getResult().toString());
        };

        Bukkit.getScheduler().runTaskAsynchronously(EnjinMinecraftPlugin.getInstance(), runnable);
    }
}
