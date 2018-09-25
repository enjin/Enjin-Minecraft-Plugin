package com.enjin.bukkit.shop;

import com.enjin.bukkit.EnjinMinecraftPlugin;
import com.enjin.bukkit.command.commands.BuyCommand;
import com.enjin.bukkit.config.EMPConfig;
import com.enjin.bukkit.util.OptionalUtil;
import com.enjin.common.shop.PlayerShopInstance;
import com.enjin.core.Enjin;
import com.enjin.core.EnjinServices;
import com.enjin.rpc.mappings.mappings.general.RPCData;
import com.enjin.rpc.mappings.mappings.shop.Shop;
import com.enjin.rpc.mappings.services.ShopService;
import com.google.common.base.Optional;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.UUID;

public class RPCShopFetcher implements Runnable {
    private EnjinMinecraftPlugin plugin;
    private UUID                 uuid;

    public RPCShopFetcher(Player player) {
        this.plugin = EnjinMinecraftPlugin.getInstance();
        this.uuid = player.getUniqueId();
    }

    @Override
    public void run() {
        Optional<Player> p = OptionalUtil.getPlayer(uuid);

        if (!p.isPresent()) {
            Enjin.getLogger().debug("Player is not present. No longer fetching shop data.");
            return;
        }

        final Player        player = p.get();
        RPCData<List<Shop>> data   = EnjinServices.getService(ShopService.class).get(player.getName());

        if (data == null) {
            player.sendMessage(ChatColor.RED + "Failed to fetch shop data.");
            return;
        }

        if (data.getError() != null) {
            player.sendMessage(ChatColor.RED + data.getError().getMessage());
            return;
        }

        List<Shop> shops = data.getResult();

        if (shops == null || shops.isEmpty()) {
            player.sendMessage(ChatColor.RED + "There are no shops available at this time.");
            return;
        }

        if (!PlayerShopInstance.getInstances().containsKey(player.getUniqueId())) {
            PlayerShopInstance.getInstances().put(player.getUniqueId(), new PlayerShopInstance(shops));
        } else {
            PlayerShopInstance.getInstances().get(player.getUniqueId()).update(shops);
        }

        PlayerShopInstance instance = PlayerShopInstance.getInstances().get(player.getUniqueId());

        if (Enjin.getConfiguration(EMPConfig.class).isUseBuyGUI()) {
            Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
                @Override
                public void run() {
                    BuyCommand.buy(player, new String[] {});
                }
            }, 0);
        } else {
            TextShopUtil.sendTextShop(player, instance, -1);
        }
    }
}
