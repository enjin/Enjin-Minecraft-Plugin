package com.enjin.sponge.managers;

import com.enjin.common.shop.PlayerShopInstance;
import com.enjin.core.Enjin;
import com.enjin.core.EnjinServices;
import com.enjin.rpc.mappings.mappings.general.RPCData;
import com.enjin.rpc.mappings.mappings.shop.Item;
import com.enjin.rpc.mappings.mappings.shop.Shop;
import com.enjin.rpc.mappings.services.ShopService;
import com.enjin.sponge.EnjinMinecraftPlugin;
import com.enjin.sponge.config.EMPConfig;
import com.enjin.sponge.shop.TextShopUtil;
import com.google.common.base.Optional;
import lombok.Getter;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class PurchaseManager {
    @Getter
    private static Map<String, Integer> pendingPurchases = new ConcurrentHashMap<>();

    public static void init() {
        PlayerShopInstance.getInstances().clear();
        //ShopListener.getGuiInstances().clear();
    }

    public static void processItemPurchase(Player player, Shop shop, Item item) {
        if (item == null) {
            player.sendMessage(Text.of(TextColors.RED, "You must select an item from a category first."));
        } else if (item.getPoints() == null || (item.getVariables() != null && !item.getVariables().isEmpty())) {
            // FIXME: 2/24/2016 text shop not displaying
            TextShopUtil.sendItemInfo(player, shop, item);
        } else {
            PurchaseManager.getPendingPurchases().put(player.getName(), item.getId());
            player.sendMessage(Text.of(TextColors.GREEN,
                                       "Type \"/",
                                       Enjin.getConfiguration(EMPConfig.class).getBuyCommand(),
                                       " confirm\" to complete your pending purchase."));
        }
    }

    public static void confirmPurchase(final Player player) {
        if (!pendingPurchases.containsKey(player.getName())) {
            player.sendMessage(Text.of(TextColors.RED, "You do not have a pending purchase."));
            return;
        }

        final Integer id = pendingPurchases.get(player.getName());
        pendingPurchases.remove(player.getName());

        Runnable runnable = () -> {
            ShopService service = EnjinServices.getService(ShopService.class);
            RPCData<Integer> data = service.purchase(player.getName(),
                                                     id,
                                                     Optional.<Map<Integer, String>>absent(),
                                                     Optional.<Integer>absent(),
                                                     Optional.<Integer>absent(),
                                                     false);

            if (data == null) {
                player.sendMessage(Text.of(TextColors.RED,
                                           "A fatal error has occurred. Please try again later. If the problem persists please contact Enjin support."));
                return;
            }

            if (data.getError() != null) {
                player.sendMessage(Text.of(TextColors.RED, data.getError().getMessage()));
                return;
            }

            player.sendMessage(Text.of(TextColors.GREEN,
                                       "You successfully purchased the item. Your new point balance is: ",
                                       data.getResult().toString()));
        };

        EnjinMinecraftPlugin.getInstance().getGame().getScheduler().createTaskBuilder()
                            .execute(runnable)
                            .submit(Enjin.getPlugin());
    }
}
