package com.enjin.officialplugin.commands.store;

import com.enjin.officialplugin.EnjinMinecraftPlugin;
import com.enjin.officialplugin.shop.PlayerShopInstance;
import com.enjin.officialplugin.shop.ShopFetcher;
import com.enjin.officialplugin.shop.ShopUtil;
import com.google.common.base.Optional;
import org.spongepowered.api.entity.player.Player;
import org.spongepowered.api.text.Texts;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.util.command.CommandException;
import org.spongepowered.api.util.command.CommandResult;
import org.spongepowered.api.util.command.CommandSource;
import org.spongepowered.api.util.command.args.CommandContext;
import org.spongepowered.api.util.command.spec.CommandExecutor;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class BuyCommand implements CommandExecutor {
    @Override
    public CommandResult execute(CommandSource source, CommandContext context) throws CommandException {
        if (!(source instanceof Player)) {
            return CommandResult.empty();
        }

        Player player = (Player) source;
        Map<UUID, PlayerShopInstance> instances = PlayerShopInstance.getInstances();
        if (!instances.containsKey(player.getUniqueId()) || shouldUpdate(instances.get(player.getUniqueId()))) {
            fetchShop((Player) source);
        } else {
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
        EnjinMinecraftPlugin.getInstance().getGame().getScheduler().getTaskBuilder()
                .async()
                .execute(new ShopFetcher(player))
                .submit(EnjinMinecraftPlugin.getInstance());
    }
}
