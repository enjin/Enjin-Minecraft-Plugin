package com.enjin.sponge.gui.impl;

import com.enjin.sponge.gui.AbstractVirtualInventory;
import lombok.NonNull;
import org.spongepowered.api.item.inventory.InventoryArchetype;
import org.spongepowered.api.item.inventory.InventoryArchetypes;
import org.spongepowered.api.text.Text;

public class VirtualChestInventory extends AbstractVirtualInventory {

    public VirtualChestInventory(@NonNull Text title, int width, int height) {
        super(title, width, height);
    }

    @Override
    public InventoryArchetype getInventoryArchetype() {
        return InventoryArchetypes.DOUBLE_CHEST;
    }

}
