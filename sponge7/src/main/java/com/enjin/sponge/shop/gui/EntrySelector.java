package com.enjin.sponge.shop.gui;

import com.enjin.rpc.mappings.mappings.shop.Category;
import com.enjin.rpc.mappings.mappings.shop.Item;
import com.enjin.rpc.mappings.mappings.shop.Shop;
import com.enjin.sponge.gui.VirtualBaseItem;
import com.enjin.sponge.gui.VirtualInventory;
import com.enjin.sponge.gui.VirtualItem;
import com.enjin.sponge.gui.impl.VirtualChestInventory;
import com.enjin.sponge.utils.ItemUtil;
import com.enjin.sponge.utils.text.TextUtils;
import lombok.NonNull;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.property.SlotPos;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.text.serializer.TextSerializers;

import java.util.List;

public class EntrySelector extends VirtualChestInventory {

    private VirtualInventory parent;
    private Shop             shop;
    private Category         category;

    public EntrySelector(@NonNull VirtualInventory parent, @NonNull Shop shop, Category category) {
        super(Text.of(TextColors.RED, category == null ? shop.getName() : category.getName()), 9, 6);
        this.parent = parent;
        this.shop = shop;
        this.category = category;
        init();
    }

    private void init() {
        List<Category> categories = category == null ? shop.getCategories() : category.getCategories();
        List<Item>     items      = category == null ? null : category.getItems();

        ItemStack stack = ItemStack.builder()
                                   .from(Items.BACK)
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

        int index = 0;
        if (categories != null && !categories.isEmpty()) {
            for (Category entry : categories) {
                stack = ItemStack.builder()
                                 .itemType(entry.getIconItem() == null
                                                   ? ItemTypes.CHEST
                                                   : Sponge.getRegistry()
                                                           .getType(ItemType.class, entry.getIconItem())
                                                           .orElse(ItemTypes.CHEST))
                                 .add(Keys.DISPLAY_NAME, TextSerializers.FORMATTING_CODE.deserialize(new StringBuilder()
                                                                                                             .append('&')
                                                                                                             .append(shop.getColorId())
                                                                                                             .append(index + 1)
                                                                                                             .append(". ")
                                                                                                             .append('&')
                                                                                                             .append(shop.getColorName())
                                                                                                             .append(entry.getName())
                                                                                                             .toString()))
                                 .add(Keys.ITEM_LORE,
                                      TextUtils.splitToListWithPrefix(entry.getInfo(), 30, "&" + shop.getColorInfo()))
                                 .build();
                if (entry.getIconDamage() != null) {
                    ItemUtil.setLegacyData(stack, entry.getIconDamage());
                }
                item = new VirtualBaseItem(stack);
                item.setPrimaryActionConsumer(snapshot -> new EntrySelector(this,
                                                                            this.shop,
                                                                            entry).open(snapshot.getPlayer()));
                register(SlotPos.of((index + 9) % getWidth(), (index + 9) / getWidth()), item);
                index++;
            }
        }

        if (items != null && !items.isEmpty()) {
            for (Item entry : items) {
                stack = ItemStack.builder()
                                 .itemType(entry.getIconItem() == null
                                                   ? ItemTypes.FILLED_MAP
                                                   : Sponge.getRegistry()
                                                           .getType(ItemType.class, entry.getIconItem())
                                                           .orElse(ItemTypes.FILLED_MAP))
                                 .add(Keys.DISPLAY_NAME, TextSerializers.FORMATTING_CODE.deserialize(new StringBuilder()
                                                                                                             .append('&')
                                                                                                             .append(shop.getColorId())
                                                                                                             .append(index + 1)
                                                                                                             .append(". ")
                                                                                                             .append('&')
                                                                                                             .append(shop.getColorName())
                                                                                                             .append(entry.getName())
                                                                                                             .toString()))
                                 .add(Keys.ITEM_LORE,
                                      TextUtils.splitToListWithPrefix(entry.getInfo(), 30, "&" + shop.getColorInfo()))
                                 .build();
                if (entry.getIconDamage() != null) {
                    ItemUtil.setLegacyData(stack, entry.getIconDamage());
                }
                item = new VirtualBaseItem(stack);
                item.setPrimaryActionConsumer(snapshot -> new ItemTransaction(this,
                                                                              this.shop,
                                                                              entry).open(snapshot.getPlayer()));
                register(SlotPos.of((index + 9) % getWidth(), (index + 9) / getWidth()), item);
                index++;
            }
        }

    }

    public static class Items {

        public static final ItemStack BACK = ItemStack.builder()
                                                      .itemType(ItemTypes.ARROW)
                                                      .build();

    }

}
