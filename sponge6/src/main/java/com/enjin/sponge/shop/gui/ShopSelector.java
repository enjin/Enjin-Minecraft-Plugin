package com.enjin.sponge.shop.gui;

import com.enjin.common.shop.PlayerShopInstance;
import com.enjin.rpc.mappings.mappings.shop.Shop;
import com.enjin.sponge.gui.VirtualBaseItem;
import com.enjin.sponge.gui.VirtualItem;
import com.enjin.sponge.gui.impl.VirtualChestInventory;
import com.enjin.sponge.utils.text.TextUtils;
import com.google.common.collect.ImmutableMap;
import lombok.NonNull;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.property.SlotPos;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.TextTemplate;
import static org.spongepowered.api.text.TextTemplate.arg;
import org.spongepowered.api.text.format.TextColors;

public class ShopSelector extends VirtualChestInventory {

    private static final TextTemplate NAME_TEMPLATE = TextTemplate.of(arg("shop").color(TextColors.DARK_GRAY));

    private PlayerShopInstance instance;

    public ShopSelector(@NonNull PlayerShopInstance instance) {
        super(Text.of(TextColors.RED, "Shop Selector"), 9, 6);
        this.instance = instance;
        init();
    }

    private void init() {
        int index = 0;
        for (Shop shop : instance.getShops()) {
            SlotPos pos = SlotPos.of(index % getWidth(), index / getWidth());
            VirtualItem item = new VirtualBaseItem(ItemStack.builder().from(Items.SHOP_SELECTION)
                                                            .add(Keys.DISPLAY_NAME, NAME_TEMPLATE
                                                                    .apply(ImmutableMap.of("shop",
                                                                                           TextUtils.translateText(new StringBuilder()
                                                                                                                           .append('&')
                                                                                                                           .append(shop.getColorId())
                                                                                                                           .append(index + 1)
                                                                                                                           .append(". ")
                                                                                                                           .append('&')
                                                                                                                           .append(shop.getColorName())
                                                                                                                           .append(shop.getName())
                                                                                                                           .toString())))
                                                                    .build())
                                                            .add(Keys.ITEM_LORE,
                                                                 TextUtils.splitToListWithPrefix(shop.getInfo(),
                                                                                                 30,
                                                                                                 "&" + shop.getColorInfo()))
                                                            .build());
            item.setPrimaryActionConsumer(snapshot -> {
                EntrySelector selector = new EntrySelector(this, shop, null);
                selector.open(snapshot.getPlayer());
            });
            register(pos, item);
            index++;
        }
    }

    public static class Items {

        public static final ItemStack SHOP_SELECTION = ItemStack.builder()
                                                                .itemType(ItemTypes.CHEST)
                                                                .build();

    }

}
