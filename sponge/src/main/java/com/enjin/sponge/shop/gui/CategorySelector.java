package com.enjin.sponge.shop.gui;

import com.enjin.rpc.mappings.mappings.shop.Category;
import com.enjin.rpc.mappings.mappings.shop.Item;
import com.enjin.rpc.mappings.mappings.shop.Shop;
import com.enjin.sponge.gui.VirtualBaseItem;
import com.enjin.sponge.gui.VirtualInventory;
import com.enjin.sponge.gui.VirtualItem;
import com.enjin.sponge.gui.impl.VirtualChestInventory;
import com.enjin.sponge.utils.ItemUtil;
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

public class CategorySelector extends VirtualChestInventory {

    private VirtualInventory parent;
    private Shop shop;
    private Category category;

    public CategorySelector(@NonNull VirtualInventory parent, @NonNull Shop shop, Category category) {
        super(Text.of(TextColors.RED, category == null ? shop.getName() : category.getName()), 9, 6);
        this.parent = parent;
        this.shop = shop;
        this.category = category;
        init();
    }

    private void init() {
        List<Category> categories = category == null ? shop.getCategories() : category.getCategories();
        List<Item> items = category == null ? null : category.getItems();

        ItemStack stack = ItemStack.builder()
                .from(Items.BACK)
                .add(Keys.DISPLAY_NAME, TextSerializers.FORMATTING_CODE.deserialize(new StringBuilder()
                        .append('&')
                        .append(shop.getColorText())
                        .append("Back")
                        .toString()))
                .build();
        VirtualItem item = new VirtualBaseItem(stack);
        item.setPrimaryActionConsumer(snapshot -> this.parent.open(snapshot.getPlayer()));
        register(SlotPos.of(0, 0), item);

        int index = 0;
        if (!categories.isEmpty()) {
            for (Category category : categories) {
                stack = ItemStack.builder()
                        .itemType(category.getIconItem() == null
                                ? ItemTypes.CHEST
                                : Sponge.getRegistry().getType(ItemType.class, category.getIconItem()).orElse(ItemTypes.CHEST))
                        .add(Keys.DISPLAY_NAME, TextSerializers.FORMATTING_CODE.deserialize(new StringBuilder()
                                .append('&')
                                .append(shop.getColorId())
                                .append(index + 1)
                                .append(". ")
                                .append('&')
                                .append(shop.getColorName())
                                .append(category.getName())
                                .toString()))
                        .build();
                if (category.getIconDamage() != null) {
                    ItemUtil.setLegacyData(stack, category.getIconDamage());
                }
                register(SlotPos.of((index + 9) % getWidth(), (index + 9) / getWidth()), new VirtualBaseItem(stack));
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
