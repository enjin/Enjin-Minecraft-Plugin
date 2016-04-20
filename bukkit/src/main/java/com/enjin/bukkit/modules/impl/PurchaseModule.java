package com.enjin.bukkit.modules.impl;

import com.enjin.bukkit.EnjinMinecraftPlugin;
import com.enjin.bukkit.modules.Module;
import com.enjin.bukkit.shop.ShopListener;
import com.enjin.bukkit.shop.TextShopUtil;
import com.enjin.common.shop.PlayerShopInstance;
import com.enjin.core.EnjinServices;
import com.enjin.rpc.mappings.mappings.general.RPCData;
import com.enjin.rpc.mappings.mappings.shop.Item;
import com.enjin.rpc.mappings.mappings.shop.Shop;
import com.enjin.rpc.mappings.services.ShopService;
import com.google.common.base.Optional;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Module(name = "Purchase")
public class PurchaseModule {
	private EnjinMinecraftPlugin plugin;
    @Getter
    private Map<String, Integer> pendingPurchases = new ConcurrentHashMap<>();

	public PurchaseModule() {
		this.plugin = EnjinMinecraftPlugin.getInstance();
	}

    public void init() {
        PlayerShopInstance.getInstances().clear();
        ShopListener.getGuiInstances().clear();
    }

    public void processItemPurchase(Player player, Shop shop, Item item) {
        if (item == null) {
            player.sendMessage(ChatColor.RED + "You must select an item from a category first.");
        } else if (item.getPoints() == null || (item.getVariables() != null && !item.getVariables().isEmpty())) {
            TextShopUtil.sendItemInfo(player, shop, item);
        } else {
            pendingPurchases.put(player.getName(), item.getId());
            player.sendMessage(ChatColor.GREEN + "Type \"/buy confirm\" to complete your pending purchase.");
        }
    }

    public void confirmPurchase(final Player player) {
        if (!pendingPurchases.containsKey(player.getName())) {
            player.sendMessage(ChatColor.RED + "You do not have a pending purchase.");
            return;
        }

        final Integer id = pendingPurchases.get(player.getName());
        pendingPurchases.remove(player.getName());

        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                ShopService service = EnjinServices.getService(ShopService.class);
                RPCData<Integer> data = service.purchase(player.getName(),
                        id,
                        Optional.<Map<Integer, String>>absent(),
                        Optional.<Integer>absent(),
                        Optional.<Integer>absent(),
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
            }
        };

        Bukkit.getScheduler().runTaskAsynchronously(EnjinMinecraftPlugin.getInstance(), runnable);
    }
}
