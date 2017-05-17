package com.enjin.sponge.gui;

import com.enjin.core.Enjin;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.event.item.inventory.ClickInventoryEvent;
import org.spongepowered.api.item.ItemType;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.text.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

public class VirtualBaseItem implements VirtualItem {

    @Getter @Setter
    private ItemType type;

    @Getter @Setter
    private Text title;

    @Getter @Setter
    private int quantity;

    private List<Text> lore;

    private Optional<Consumer<VirtualInventoryEventSnapshot.Click>> primaryActionConsumer = Optional.empty();
    private Optional<Consumer<VirtualInventoryEventSnapshot.Click>> secondaryActionConsumer = Optional.empty();

    public VirtualBaseItem(@NonNull ItemType type, @NonNull Text title, int quantity, @NonNull List<Text> lore) {
        this.type = type;
        this.title = title;
        this.quantity = quantity;
        this.lore = lore;
    }

    public VirtualBaseItem(ItemStack item) {
        this(item.getItem(),
                item.get(Keys.DISPLAY_NAME).orElse(Text.of()),
                item.getQuantity(),
                item.get(Keys.ITEM_LORE).orElse(new ArrayList<>()));
    }

    @Override
    public ItemStack createItemStack() {
        return ItemStack.builder()
                .itemType(getType())
                .add(Keys.DISPLAY_NAME, getTitle())
                .quantity(getQuantity())
                .build();
    }

    @Override
    public void setPrimaryActionConsumer(Consumer<VirtualInventoryEventSnapshot.Click> primaryActionConsumer) {
        this.primaryActionConsumer = Optional.ofNullable(primaryActionConsumer);
    }

    @Override
    public void setSecondaryActionConsumer(Consumer<VirtualInventoryEventSnapshot.Click> secondaryActionConsumer) {
        this.secondaryActionConsumer = Optional.ofNullable(secondaryActionConsumer);
    }

    @Override
    public void onClick(@NonNull VirtualInventoryEventSnapshot.Click snapshot) {
        Enjin.getLogger().debug("VirtualBaseItem#onClick");
        if (snapshot.getEvent() instanceof ClickInventoryEvent.Primary) {
            Enjin.getLogger().debug("VirtualBaseItem#onClick - Primary");
            if (primaryActionConsumer.isPresent()) {
                Enjin.getLogger().debug("VirtualBaseItem#onClick - Primary, Consuming Action");
                primaryActionConsumer.get().accept(snapshot);
            }
        } else if (snapshot.getEvent() instanceof ClickInventoryEvent.Secondary) {
            Enjin.getLogger().debug("VirtualBaseItem#onClick - Secondary");
            if (secondaryActionConsumer.isPresent()) {
                Enjin.getLogger().debug("VirtualBaseItem#onClick - Secondary, Consuming Action");
                secondaryActionConsumer.get().accept(snapshot);
            }
        }
    }

}
