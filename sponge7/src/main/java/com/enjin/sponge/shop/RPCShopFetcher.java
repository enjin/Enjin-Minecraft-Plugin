package com.enjin.sponge.shop;

import com.enjin.common.shop.PlayerShopInstance;
import com.enjin.core.Enjin;
import com.enjin.core.EnjinServices;
import com.enjin.sponge.EnjinMinecraftPlugin;
import com.enjin.rpc.mappings.mappings.general.RPCData;
import com.enjin.rpc.mappings.mappings.shop.Shop;
import com.enjin.rpc.mappings.services.ShopService;
import com.enjin.sponge.command.commands.BuyCommand;
import com.enjin.sponge.config.EMPConfig;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class RPCShopFetcher implements Runnable {
    private EnjinMinecraftPlugin plugin;
    private UUID uuid;

    public RPCShopFetcher(Player player) {
        this.plugin = EnjinMinecraftPlugin.getInstance();
        this.uuid = player.getUniqueId();
    }

    @Override
    public void run() {
        Optional<Player> p = plugin.getGame().getServer().getPlayer(uuid);

        if (!p.isPresent()) {
            Enjin.getLogger().debug("Player is not present. No longer fetching shop data.");
            return;
        }

        Player player = p.get();
        RPCData<List<Shop>> data = EnjinServices.getService(ShopService.class).get(player.getName());

        if (data == null) {
            player.sendMessage(Text.builder("Failed to fetch shop data.").color(TextColors.RED).build());
            return;
        }

        if (data.getError() != null) {
            player.sendMessage(Text.of(data.getError().getMessage()));
            return;
        }

        List<Shop> shops = data.getResult();

        if (shops == null || shops.isEmpty()) {
            player.sendMessage(Text.builder("There are no shops available at this time.").color(TextColors.RED).build());
            return;
        }

        if (!PlayerShopInstance.getInstances().containsKey(player.getUniqueId())) {
            PlayerShopInstance.getInstances().put(player.getUniqueId(), new PlayerShopInstance(shops));
        } else {
            PlayerShopInstance.getInstances().get(player.getUniqueId()).update(shops);
        }

        PlayerShopInstance instance = PlayerShopInstance.getInstances().get(player.getUniqueId());

        if (Enjin.getConfiguration(EMPConfig.class).isUseBuyGUI()) {
            EnjinMinecraftPlugin.getInstance().getSync().schedule(() -> {
                BuyCommand.buy(player, new String[]{});
            }, 0, TimeUnit.SECONDS);
        } else {
            TextShopUtil.sendTextShop(player, instance, -1);
        }
    }
}
