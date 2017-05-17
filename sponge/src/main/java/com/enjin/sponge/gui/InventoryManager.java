package com.enjin.sponge.gui;

import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.item.inventory.InventoryArchetype;

public interface InventoryManager {

    InventoryArchetype getInventoryArchetype();

    InventoryListener getInventoryListener(Player player);

    Inventory.Builder buildInventory(Player player, InventoryListener listener);

    Inventory createInventory(Player player);

    Inventory populateInventory(Inventory inventory);

}
