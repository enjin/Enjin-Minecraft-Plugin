package com.enjin.sponge.gui;

import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.text.Text;

import java.util.function.Consumer;

public interface VirtualItem {

    ItemType getType();

    void setType(ItemType type);

    Text getTitle();

    void setTitle(Text title);

    int getQuantity();

    void setQuantity(int quantity);

    ItemStack createItemStack();

    void setPrimaryActionConsumer(Consumer<VirtualInventoryEventSnapshot.Click> primaryActionConsumer);

    void setSecondaryActionConsumer(Consumer<VirtualInventoryEventSnapshot.Click> secondaryActionConsumer);

    void onClick(VirtualInventoryEventSnapshot.Click snapshot);

}
