package com.enjin.sponge.gui;

import org.spongepowered.api.event.item.inventory.ClickInventoryEvent;
import org.spongepowered.api.event.item.inventory.InteractInventoryEvent;
import org.spongepowered.api.item.inventory.Inventory;

public interface InventoryListener {

    void updateSlotMaps(Inventory inventory);

    void onOpen(InteractInventoryEvent.Open event);

    void onClose(InteractInventoryEvent.Close event);

    void onClick(ClickInventoryEvent event);

}
