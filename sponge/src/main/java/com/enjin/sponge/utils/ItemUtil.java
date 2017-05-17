package com.enjin.sponge.utils;

import lombok.NonNull;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.type.DyeColor;
import org.spongepowered.api.data.type.DyeColors;
import org.spongepowered.api.item.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;

public class ItemUtil {

    private static final Map<Integer, DyeColor> DYES = new HashMap<Integer, DyeColor>() {{
        put(0, DyeColors.BLACK);
        put(1, DyeColors.RED);
        put(2, DyeColors.GREEN);
        put(3, DyeColors.BROWN);
        put(4, DyeColors.BLUE);
        put(5, DyeColors.PURPLE);
        put(6, DyeColors.CYAN);
        put(7, DyeColors.SILVER);
        put(8, DyeColors.GRAY);
        put(9, DyeColors.PINK);
        put(10, DyeColors.LIME);
        put(11, DyeColors.YELLOW);
        put(12, DyeColors.LIGHT_BLUE);
        put(13, DyeColors.MAGENTA);
        put(14, DyeColors.ORANGE);
        put(15, DyeColors.WHITE);
    }};

    public static void setLegacyData(@NonNull ItemStack stack, byte data) {
        if (stack.supports(Keys.DYE_COLOR)) {
            if (data < 0)
                data = 0;
            else if (data > 15)
                data = 15;
            stack.offer(Keys.DYE_COLOR, DYES.get(data));
        }
    }

}
