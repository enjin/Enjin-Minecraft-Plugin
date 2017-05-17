package com.enjin.sponge.gui;

import com.enjin.core.Enjin;
import lombok.Getter;
import lombok.NonNull;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.cause.NamedCause;
import org.spongepowered.api.event.item.inventory.ClickInventoryEvent;
import org.spongepowered.api.event.item.inventory.InteractInventoryEvent;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.item.inventory.property.InventoryDimension;
import org.spongepowered.api.item.inventory.property.InventoryTitle;
import org.spongepowered.api.item.inventory.property.SlotPos;
import org.spongepowered.api.text.Text;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class AbstractVirtualInventory implements VirtualInventory, InventoryManager {

    private final Map<SlotPos, VirtualItem> registeredVirtualItems = new HashMap<>();

    @Getter
    private Text title;

    @Getter
    private int width;
    @Getter
    private int height;

    protected AbstractVirtualInventory(@NonNull Text title, int width, int height) {
        this.title = title;
        this.width = width;
        this.height = height;
    }

    public Map<SlotPos, VirtualItem> getSlotPosMappedVirtualItems() {
        return new HashMap<>(this.registeredVirtualItems);
    }

    public List<VirtualItem> getVirtualItems() {
        return new ArrayList<>(this.registeredVirtualItems.values());
    }

    @Override
    public void open(@NonNull Player player) {
        if (player.isViewingInventory()) {
            player.closeInventory(Cause.of(NamedCause.owner(Enjin.getPlugin())));
        }
        Inventory inventory = createInventory(player);
        player.openInventory(inventory, Cause.of(NamedCause.owner(Enjin.getPlugin())));
    }

    @Override
    public void register(SlotPos pos, VirtualItem item) {
        this.registeredVirtualItems.put(pos, item);
    }

    @Override
    public InventoryListener getInventoryListener(Player player) {
        return new GenericInventoryListener(player, this);
    }

    @Override
    public Inventory.Builder buildInventory(Player player, InventoryListener listener) {
        return Inventory.builder()
                .of(getInventoryArchetype())
                .property(InventoryTitle.PROPERTY_NAME, InventoryTitle.of(this.title))
                .property(InventoryDimension.PROPERTY_NAM, InventoryDimension.of(this.width, this.height))
                .withCarrier(player)
                .listener(InteractInventoryEvent.Open.class, listener::onOpen)
                .listener(InteractInventoryEvent.Close.class, listener::onClose)
                .listener(ClickInventoryEvent.class, listener::onClick);
    }

    @Override
    public Inventory createInventory(Player player) {
        InventoryListener listener = getInventoryListener(player);
        Inventory inventory = populateInventory(buildInventory(player, listener).build(Enjin.getPlugin()));
        listener.updateSlotMaps(inventory);
        return inventory;
    }

    @Override
    public Inventory populateInventory(@NonNull Inventory inventory) {
        for (Map.Entry<SlotPos, VirtualItem> entry : getSlotPosMappedVirtualItems().entrySet()) {
            inventory.query(entry.getKey()).set(entry.getValue().createItemStack());
        }
        return inventory;
    }

}
