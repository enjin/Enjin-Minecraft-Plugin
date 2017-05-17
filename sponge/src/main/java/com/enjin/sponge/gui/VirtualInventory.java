package com.enjin.sponge.gui;

import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.item.inventory.property.SlotPos;

import java.util.List;
import java.util.Map;

public interface VirtualInventory {

    Map<SlotPos, VirtualItem> getSlotPosMappedVirtualItems();

    List<VirtualItem> getVirtualItems();

    int getWidth();

    int getHeight();

    void open(Player player);

    void register(SlotPos pos, VirtualItem item);

}
