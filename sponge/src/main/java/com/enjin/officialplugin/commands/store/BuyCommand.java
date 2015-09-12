package com.enjin.officialplugin.commands.store;

import com.enjin.officialplugin.EnjinMinecraftPlugin;
import com.enjin.officialplugin.shop.PlayerShopInstance;
import com.enjin.officialplugin.shop.LegacyShopFetcher;
import com.enjin.officialplugin.shop.RPCShopFetcher;
import com.enjin.officialplugin.shop.ShopUtil;
import com.enjin.rpc.mappings.mappings.shop.Category;
import com.google.common.base.Optional;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Texts;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.util.command.CommandException;
import org.spongepowered.api.util.command.CommandResult;
import org.spongepowered.api.util.command.CommandSource;
import org.spongepowered.api.util.command.args.CommandContext;
import org.spongepowered.api.util.command.spec.CommandExecutor;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class BuyCommand implements CommandExecutor {
    @Override
    public CommandResult execute(CommandSource source, CommandContext context) throws CommandException {
        EnjinMinecraftPlugin plugin = EnjinMinecraftPlugin.getInstance();

        if (!(source instanceof Player)) {
            return CommandResult.empty();
        }

        Optional<Integer> selection = context.getOne("#");

        Player player = (Player) source;
        Map<UUID, PlayerShopInstance> instances = PlayerShopInstance.getInstances();
        if (!instances.containsKey(player.getUniqueId()) || shouldUpdate(instances.get(player.getUniqueId()))) {
            fetchShop((Player) source);
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
                                player.sendMessage(Texts.builder("There are no items in this category.").color(TextColors.RED).build());
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

                            return CommandResult.success();
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

        return CommandResult.success();
    }

    private static boolean shouldUpdate(PlayerShopInstance instance) {
        return (System.currentTimeMillis() - instance.getLastUpdated()) > TimeUnit.MINUTES.toMillis(10);
    }

    public static class ShopCommand implements CommandExecutor {
        @Override
        public CommandResult execute(CommandSource source, CommandContext context) throws CommandException {
            if (!(source instanceof Player)) {
                return CommandResult.empty();
            }

            Player player = (Player) source;
            Map<UUID, PlayerShopInstance> instances = PlayerShopInstance.getInstances();
            if (!instances.containsKey(player.getUniqueId())) {
                fetchShop((Player) source);
                return CommandResult.empty();
            }

            Optional<Integer> index = context.getOne("#");
            if (!index.isPresent()) {
                return CommandResult.empty();
            } else {
                int value = index.get() < 1 ? -1 : index.get() - 1;
                PlayerShopInstance instance = instances.get(player.getUniqueId());
                instance.updateShop(value);
                ShopUtil.sendTextShop(player, instance, -1);
            }

            return CommandResult.success();
        }
    }

    private static void fetchShop(Player player) {
        EnjinMinecraftPlugin.getInstance().getGame().getScheduler().createTaskBuilder()
                .async()
                .execute(EnjinMinecraftPlugin.getInstance().getConfig().isLegacy() ? new LegacyShopFetcher(player) : new RPCShopFetcher(player))
                .submit(EnjinMinecraftPlugin.getInstance());
    }
}
