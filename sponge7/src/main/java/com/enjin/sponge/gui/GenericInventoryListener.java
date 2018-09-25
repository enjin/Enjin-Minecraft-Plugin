package com.enjin.sponge.gui;

import com.enjin.core.Enjin;
import com.enjin.sponge.EnjinMinecraftPlugin;
import com.enjin.sponge.utils.InventoryUtil;
import lombok.NonNull;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.item.inventory.ClickInventoryEvent;
import org.spongepowered.api.event.item.inventory.InteractInventoryEvent;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.item.inventory.Slot;
import org.spongepowered.api.item.inventory.property.SlotPos;
import org.spongepowered.api.item.inventory.transaction.SlotTransaction;

import java.util.Comparator;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;

public class GenericInventoryListener implements InventoryListener {

    private static final Comparator<Slot> SLOT_COMPARATOR = Comparator.comparingInt(InventoryUtil::getSlotOrdinal);

    private final Map<Slot, SlotPos> slotToSlotPos = new TreeMap<>(SLOT_COMPARATOR);

    private Player           player;
    private VirtualInventory inventory;

    public GenericInventoryListener(@NonNull Player player, VirtualInventory inventory) {
        this.player = player;
        this.inventory = inventory;
    }

    @Override
    public void updateSlotMaps(Inventory inventory) {
        this.slotToSlotPos.clear();

        int index = 0;
        for (Slot slot : inventory.<Slot>slots()) {
            SlotPos pos = SlotPos.of(index % this.inventory.getWidth(), index / this.inventory.getWidth());
            this.slotToSlotPos.put(slot, pos);
            index++;
        }
    }

    @Override
    public void onOpen(InteractInventoryEvent.Open event) {
    }

    @Override
    public void onClose(InteractInventoryEvent.Close event) {
    }

    @Override
    public void onClick(ClickInventoryEvent event) {
        Inventory target = event.getTargetInventory();
        for (SlotTransaction transaction : event.getTransactions()) {
            Slot slot = transaction.getSlot();
            if (InventoryUtil.isSlotInInventory(slot, target)) {
                event.setCancelled(true);
                if (this.slotToSlotPos.containsKey(slot)) {
                    SlotPos                   pos                  = this.slotToSlotPos.get(slot);
                    Map<SlotPos, VirtualItem> slotPosToVirtualItem = this.inventory.getSlotPosMappedVirtualItems();
                    if (slotPosToVirtualItem.containsKey(pos)) {
                        Enjin.getLogger().debug("GenericInventoryListener#onClick - VirtualItem Exists");
                        final VirtualItem item = slotPosToVirtualItem.get(pos);
                        final VirtualInventoryEventSnapshot.Click snapshot = VirtualInventoryEventSnapshot.Click
                                .of(event, this.inventory, this.player, slot, pos, item);
                        EnjinMinecraftPlugin.getInstance().getSync()
                                            .schedule(() -> item.onClick(snapshot), 50, TimeUnit.MILLISECONDS);
                    }
                }
            }
        }
    }

}
