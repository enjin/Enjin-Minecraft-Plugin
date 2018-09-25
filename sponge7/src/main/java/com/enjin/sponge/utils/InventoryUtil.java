package com.enjin.sponge.utils;

import com.google.common.base.Throwables;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.item.inventory.Slot;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class InventoryUtil {

    private static final Class<?>     SLOT_ADAPTER_CLASS;
    private static final MethodHandle GET_ORDINAL;

    static {
        try {
            MethodType getOrdinalMethodType = MethodType.methodType(int.class);
            SLOT_ADAPTER_CLASS = Class.forName("org.spongepowered.common.item.inventory.adapter.impl.slots.SlotAdapter");
            GET_ORDINAL = MethodHandles.publicLookup()
                                       .findVirtual(SLOT_ADAPTER_CLASS, "getOrdinal", getOrdinalMethodType);
        } catch (ReflectiveOperationException e) {
            throw Throwables.propagate(e);
        }
    }

    public static boolean isSlotInInventory(Slot slot, Inventory inventory) {
        Inventory parent = slot.parent();
        return parent.equals(inventory) || parent.equals(inventory.first());
    }

    public static int getSlotOrdinal(Slot slot) {
        try {
            return (int) GET_ORDINAL.invoke(slot);
        } catch (Throwable t) {
            throw new UnsupportedOperationException(t);
        }
    }

}
