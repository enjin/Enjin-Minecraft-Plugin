package com.enjin.bukkit.shop;

import com.enjin.bukkit.util.OptionalUtil;
import com.enjin.core.EnjinServices;
import com.enjin.bukkit.EnjinMinecraftPlugin;
import com.enjin.officialplugin.shop.PlayerShopInstance;
import com.enjin.rpc.mappings.mappings.general.RPCData;
import com.enjin.rpc.mappings.mappings.shop.Shop;
import com.enjin.rpc.mappings.services.ShopService;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ComponentBuilder;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class RPCShopFetcher implements Runnable {
    private EnjinMinecraftPlugin plugin;
    private UUID uuid;

    public RPCShopFetcher(Player player) {
        this.plugin = EnjinMinecraftPlugin.instance;
        this.uuid = player.getUniqueId();
    }

    @Override
    public void run() {
        Optional<Player> p = OptionalUtil.getPlayer(uuid);

        if (!p.isPresent()) {
            plugin.debug("Player is not present. No longer fetching shop data.");
            return;
        }

        Player player = p.get();
        RPCData<List<Shop>> data = EnjinServices.getService(ShopService.class).get(plugin.getAuthKey(), player.getName());

        if (data == null) {
            player.spigot().sendMessage(new ComponentBuilder("Failed to fetch shop data.").color(ChatColor.RED).create());
            return;
        }

        if (data.getError() != null) {
            player.spigot().sendMessage(new ComponentBuilder(data.getError().getMessage()).create());
            return;
        }

        List<Shop> shops = data.getResult();

        if (shops == null || shops.isEmpty()) {
            player.spigot().sendMessage(new ComponentBuilder("There are no shops available at this time.").color(ChatColor.RED).create());
            return;
        }

        if (!PlayerShopInstance.getInstances().containsKey(player.getUniqueId())) {
            PlayerShopInstance.getInstances().put(player.getUniqueId(), new PlayerShopInstance(shops));
        } else {
            PlayerShopInstance.getInstances().get(player.getUniqueId()).update(shops);
        }

        PlayerShopInstance instance = PlayerShopInstance.getInstances().get(player.getUniqueId());
        ShopUtil.sendTextShop(player, instance, -1);
    }
}
