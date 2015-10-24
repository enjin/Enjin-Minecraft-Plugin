package com.enjin.officialplugin.commands.store;

import com.enjin.officialplugin.EnjinMinecraftPlugin;
import com.enjin.officialplugin.shop.PlayerShopInstance;
import com.enjin.officialplugin.shop.RPCShopFetcher;
import com.enjin.officialplugin.shop.ShopUtil;
import com.enjin.rpc.mappings.mappings.shop.Category;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.concurrent.TimeUnit;

public class BuyCommand {
    public static void buy(Player player, String[] args) {
        EnjinMinecraftPlugin plugin = EnjinMinecraftPlugin.instance;

        if (args.length > 0 && args[0].equalsIgnoreCase("shop")) {
            BuyCommand.shop(player, args.length > 1 ? Arrays.copyOfRange(args, 1, args.length) : new String[]{});
            return;
        }

        Optional<Integer> selection;
        try {
            selection = args.length == 0 ? Optional.empty() : Optional.ofNullable(Integer.parseInt(args[0]));
        } catch (NumberFormatException e) {
            player.sendMessage(ChatColor.RED + "USAGE: /buy #");
            return;
        }

        Map<UUID, PlayerShopInstance> instances = PlayerShopInstance.getInstances();
        if (!instances.containsKey(player.getUniqueId()) || shouldUpdate(instances.get(player.getUniqueId()))) {
            fetchShop(player);
        } else {
            PlayerShopInstance instance = instances.get(player.getUniqueId());
            if (selection.isPresent()) {
                int number = selection.get() < 1 ? 1 : selection.get();
                if (instance.getActiveShop() != null) {
                    if (instance.getActiveCategory() != null) {
                        Category category = instance.getActiveCategory();
                        if (category.getCategories() != null && !category.getCategories().isEmpty()) {
                            if (instance.getActiveCategory().getCategories().size() < number) {
                                instance.updateCategory(instance.getActiveCategory().getCategories().size() - 1);
                            } else {
                                instance.updateCategory(number - 1);
                            }
                        } else {
                            if (instance.getActiveCategory().getItems().size() == 0) {
                                plugin.debug("No items found in category: " + category.getName());
                                player.sendMessage(ChatColor.RED.toString() + "There are no items in this category.");
                            } else if (number == 0) {
                                plugin.debug("Sending first item to " + player.getName());
                                ShopUtil.sendItemInfo(player, instance, 0);
                            } else if (instance.getActiveCategory().getItems().size() < number) {
                                plugin.debug("Sending last item to " + player.getName());
                                ShopUtil.sendItemInfo(player, instance, category.getItems().size() - 1);
                            } else {
                                plugin.debug("Sending item to " + player.getName());
                                ShopUtil.sendItemInfo(player, instance, number - 1);
                            }

                            return;
                        }
                    } else {
                        plugin.debug("No active category has been set. Selecting category from shop.");
                        List<Category> categories = instance.getActiveShop().getCategories();
                        if (categories.size() < number) {
                            instance.updateCategory(categories.size() - 1);
                        } else {
                            instance.updateCategory(number - 1);
                        }
                    }
                } else {
                    if (instance.getShops().size() < number) {
                        instance.updateShop(instance.getShops().size() - 1);
                    } else {
                        instance.updateShop(number - 1);
                    }
                }
            } else {
                if (instance.getActiveCategory() != null) {
                    instance.updateCategory(-1);
                }
            }

            ShopUtil.sendTextShop(player, instances.get(player.getUniqueId()), -1);
        }
    }

    public static void shop(Player player, String[] args){
        Map<UUID, PlayerShopInstance> instances = PlayerShopInstance.getInstances();
        if (!instances.containsKey(player.getUniqueId())) {
            fetchShop(player);
            return;
        }

        Optional<Integer> selection;
        try {
            selection = args.length == 0 ? Optional.empty() : Optional.ofNullable(Integer.parseInt(args[0]));
        } catch (NumberFormatException e) {
            player.sendMessage(ChatColor.RED + "USAGE: /buy #");
            return;
        }

        PlayerShopInstance instance = instances.get(player.getUniqueId());
        if (!selection.isPresent()) {
            instance.updateShop(-1);
            ShopUtil.sendTextShop(player, instance, -1);
            return;
        } else {
            int value = selection.get() < 1 ? -1 : selection.get() - 1;
            instance.updateShop(value);
            ShopUtil.sendTextShop(player, instance, -1);
        }

        return;
    }

    private static boolean shouldUpdate(PlayerShopInstance instance) {
        return (System.currentTimeMillis() - instance.getLastUpdated()) > TimeUnit.MINUTES.toMillis(10);
    }

    private static void fetchShop(Player player) {
        Bukkit.getScheduler().runTaskAsynchronously(EnjinMinecraftPlugin.instance, new RPCShopFetcher(player));
    }
}
