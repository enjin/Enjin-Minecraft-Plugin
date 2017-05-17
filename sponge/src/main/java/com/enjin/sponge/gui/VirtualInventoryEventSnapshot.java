package com.enjin.sponge.gui;

import lombok.Getter;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Event;
import org.spongepowered.api.event.item.inventory.ClickInventoryEvent;
import org.spongepowered.api.event.item.inventory.InteractInventoryEvent;
import org.spongepowered.api.item.inventory.Slot;
import org.spongepowered.api.item.inventory.property.SlotPos;

public class VirtualInventoryEventSnapshot<T extends Event> {

    @Getter
    protected T event;
    @Getter
    protected VirtualInventory inventory;
    @Getter
    protected Player player;

    protected VirtualInventoryEventSnapshot(T event, VirtualInventory inventory, Player player) {
        this.event = event;
        this.inventory = inventory;
        this.player = player;
    }

    public static class Open extends VirtualInventoryEventSnapshot<InteractInventoryEvent.Open> {

        protected Open(InteractInventoryEvent.Open event, VirtualInventory inventory, Player player) {
            super(event, inventory, player);
        }

        public static Open of(InteractInventoryEvent.Open event, VirtualInventory inventory, Player player) {
            return new Open(event, inventory, player);
        }

    }

    public static class Close extends VirtualInventoryEventSnapshot<InteractInventoryEvent.Close> {

        protected Close(InteractInventoryEvent.Close event, VirtualInventory inventory, Player player) {
            super(event, inventory, player);
        }

        public static Close of(InteractInventoryEvent.Close event, VirtualInventory inventory, Player player) {
            return new Close(event, inventory, player);
        }

    }

    public static class Click extends VirtualInventoryEventSnapshot<ClickInventoryEvent> {

        @Getter
        private Slot slot;
        @Getter
        private SlotPos position;
        @Getter
        private VirtualItem item;

        protected Click(ClickInventoryEvent event,
                        VirtualInventory inventory,
                        Player player,
                        Slot slot,
                        SlotPos position,
                        VirtualItem item) {
            super(event, inventory, player);
            this.slot = slot;
            this.position = position;
            this.item = item;
        }

        public static Click of(ClickInventoryEvent event,
                               VirtualInventory inventory,
                               Player player,
                               Slot slot,
                               SlotPos position,
                               VirtualItem item) {
            return new Click(event, inventory, player, slot, position, item);
        }

    }

}
