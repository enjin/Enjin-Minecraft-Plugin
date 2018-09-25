package com.enjin.sponge.shop.gui;

import com.enjin.core.Enjin;
import com.enjin.rpc.mappings.mappings.shop.Item;
import com.enjin.rpc.mappings.mappings.shop.Shop;
import com.enjin.sponge.gui.VirtualBaseItem;
import com.enjin.sponge.gui.VirtualInventory;
import com.enjin.sponge.gui.VirtualItem;
import com.enjin.sponge.gui.impl.VirtualChestInventory;
import com.enjin.sponge.shop.TextShopUtil;
import com.enjin.sponge.utils.text.TextUtils;
import lombok.NonNull;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.NamedCause;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.property.SlotPos;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.text.serializer.TextSerializers;

import java.text.DecimalFormat;
import java.util.ArrayList;

public class ItemTransaction extends VirtualChestInventory {

    private static final DecimalFormat PRICE_FORMAT = new DecimalFormat("#.00");

    private VirtualInventory parent;
    private Shop             shop;
    private Item             item;

    public ItemTransaction(@NonNull VirtualChestInventory parent, @NonNull Shop shop, @NonNull Item item) {
        super(Text.of(TextColors.RED, item.getName()), 9, 6);
        this.parent = parent;
        this.shop = shop;
        this.item = item;
        try {
            init();
        } catch (Exception e) {
            Enjin.getLogger().log(e);
        }
    }

    private void init() {
        ItemStack stack = ItemStack.builder()
                                   .from(EntrySelector.Items.BACK)
                                   .add(Keys.DISPLAY_NAME,
                                        TextSerializers.FORMATTING_CODE.deserialize(new StringBuilder()
                                                                                            .append('&')
                                                                                            .append(shop.getColorText())
                                                                                            .append("Back")
                                                                                            .toString()))
                                   .build();
        VirtualItem item = new VirtualBaseItem(stack);
        item.setPrimaryActionConsumer(snapshot -> this.parent.open(snapshot.getPlayer()));
        register(SlotPos.of(0, 0), item);

        stack = ItemStack.builder()
                         .itemType(ItemTypes.FILLED_MAP)
                         .add(Keys.DISPLAY_NAME, TextSerializers.FORMATTING_CODE.deserialize(new StringBuilder()
                                                                                                     .append('&')
                                                                                                     .append(shop.getColorName())
                                                                                                     .append(this.item.getName())
                                                                                                     .toString()))
                         .add(Keys.ITEM_LORE,
                              TextUtils.splitToListWithPrefix(this.item.getInfo(), 30, "&" + this.shop.getColorInfo()))
                         .build();
        register(SlotPos.of(4, 0), new VirtualBaseItem(stack));

        if (this.item.getPoints() != null) {
            int points = this.item.getPoints();
            stack = ItemStack.builder()
                             .itemType(ItemTypes.EMERALD)
                             .add(Keys.DISPLAY_NAME, TextSerializers.FORMATTING_CODE.deserialize(new StringBuilder()
                                                                                                         .append('&')
                                                                                                         .append(shop.getColorText())
                                                                                                         .append("Buy with Points")
                                                                                                         .toString()))
                             .add(Keys.ITEM_LORE, new ArrayList<Text>() {{
                                 add(TextSerializers.FORMATTING_CODE.deserialize(new StringBuilder()
                                                                                         .append('&')
                                                                                         .append(shop.getColorText())
                                                                                         .append("POINTS: ")
                                                                                         .append('&')
                                                                                         .append(shop.getColorPrice())
                                                                                         .append(points == 0 ? "FREE" : points)
                                                                                         .toString()));
                             }})
                             .build();
            item = new VirtualBaseItem(stack);
            item.setPrimaryActionConsumer(snapshot -> {
                snapshot.getPlayer().closeInventory(Cause.of(NamedCause.owner(Enjin.getPlugin())));
                TextShopUtil.sendItemInfo(snapshot.getPlayer(), shop, this.item);
            });
            register(SlotPos.of(7, 0), item);
        }

        if (this.item.getPrice() != null) {
            double price = this.item.getPrice();
            stack = ItemStack.builder()
                             .itemType(ItemTypes.DIAMOND)
                             .add(Keys.DISPLAY_NAME, TextSerializers.FORMATTING_CODE.deserialize(new StringBuilder()
                                                                                                         .append('&')
                                                                                                         .append(shop.getColorText())
                                                                                                         .append("Buy with Money")
                                                                                                         .toString()))
                             .add(Keys.ITEM_LORE, new ArrayList<Text>() {{
                                 add(TextSerializers.FORMATTING_CODE.deserialize(new StringBuilder()
                                                                                         .append('&')
                                                                                         .append(shop.getColorText())
                                                                                         .append("PRICE: ")
                                                                                         .append('&')
                                                                                         .append(shop.getColorPrice())
                                                                                         .append(price == 0.0 ? "FREE" : PRICE_FORMAT
                                                                                                 .format(price) + " " + shop
                                                                                                 .getCurrency())
                                                                                         .toString()));
                             }})
                             .build();
            item = new VirtualBaseItem(stack);
            item.setPrimaryActionConsumer(snapshot -> {
                snapshot.getPlayer().closeInventory(Cause.of(NamedCause.owner(Enjin.getPlugin())));
                TextShopUtil.sendItemInfo(snapshot.getPlayer(), shop, this.item);
            });
            register(SlotPos.of(8, 0), item);
        }
    }

}
