package com.enjin.bukkit.cmd.legacy.commands;

import com.enjin.bukkit.EnjinMinecraftPlugin;
import com.enjin.bukkit.cmd.legacy.Command;
import com.enjin.bukkit.cmd.legacy.Directive;
import com.enjin.bukkit.config.EMPConfig;
import com.enjin.bukkit.modules.impl.PurchaseModule;
import com.enjin.bukkit.shop.RPCShopFetcher;
import com.enjin.bukkit.shop.ShopListener;
import com.enjin.bukkit.shop.TextShopUtil;
import com.enjin.bukkit.shop.gui.ShopList;
import com.enjin.bukkit.util.ui.Menu;
import com.enjin.common.shop.PlayerShopInstance;
import com.enjin.core.Enjin;
import com.enjin.rpc.mappings.mappings.shop.Category;
import com.enjin.rpc.mappings.mappings.shop.Item;
import com.google.common.base.Optional;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class BuyCommand {
    @Directive(parent = "buy", value = "shop")
    public static void shop(Player player, String[] args) {
        Map<UUID, PlayerShopInstance> instances = PlayerShopInstance.getInstances();
        if (!instances.containsKey(player.getUniqueId())) {
            fetchShop(player);
            return;
        }

        Optional<Integer> selection;
        try {
            selection = args.length == 0 ? Optional.<Integer>absent() : Optional.fromNullable(Integer.parseInt(args[0]));
        } catch (NumberFormatException e) {
            player.sendMessage(ChatColor.RED + new StringBuilder("USAGE: /")
                    .append(Enjin.getConfiguration(EMPConfig.class).getBuyCommand())
                    .append(" shop #")
                    .toString());
            return;
        }

        if (Enjin.getConfiguration(EMPConfig.class).isUseBuyGUI()) {
            Menu menu = new ShopList(player);
            menu.openMenu(player);

            return;
        }

        PlayerShopInstance instance = instances.get(player.getUniqueId());
        if (!selection.isPresent()) {
            instance.updateShop(-1);
            TextShopUtil.sendTextShop(player, instance, -1);
        } else {
            int value = selection.get() < 1 ? -1 : selection.get() - 1;
            instance.updateShop(value);
            TextShopUtil.sendTextShop(player, instance, -1);
        }

    }

    private static boolean shouldUpdate(PlayerShopInstance instance) {
        return (System.currentTimeMillis() - instance.getLastUpdated()) > TimeUnit.MINUTES.toMillis(10);
    }

    private static void fetchShop(Player player) {
        Bukkit.getScheduler().runTaskAsynchronously(EnjinMinecraftPlugin.getInstance(), new RPCShopFetcher(player));
    }
}
