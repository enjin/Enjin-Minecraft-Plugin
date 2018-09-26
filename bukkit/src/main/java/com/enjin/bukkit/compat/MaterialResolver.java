package com.enjin.bukkit.compat;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class MaterialResolver {

    private static final String[] STONE_TYPES = {
            "stone",
            "granite",
            "polished_granite",
            "diorite",
            "polished_diorite",
            "andesite",
            "polished_andesite"
    };

    private static final String[] DOUBLE_STONE_SLAB = {
            "stone_brick_slab",
            "nether_brick_slab",
            "quartz_slab",
            "smooth_stone",
            "smooth_sandstone",
            "smooth_quartz"
    };

    private static final String[] COLORABLES = {
            "wool",
            "carpet",
            "bed",
            "banner",
            "stained_hardened_clay",
            "stained_glass",
            "stained_glass_pane",
            "concrete",
            "concrete_powder"
    };

    private static final String[] COLORS = {
            "white",
            "orange",
            "magenta",
            "light_blue",
            "yellow",
            "lime",
            "pink",
            "gray",
            "light_gray",
            "cyan",
            "purple",
            "blue",
            "brown",
            "green",
            "red",
            "black"
    };

    private static final String[] DYES = {
            "ink_sac",
            "rose_red",
            "cactus_green",
            "cocoa_beans",
            "lapis_lazuli",
            "purple_dye",
            "cyan_dye",
            "light_gray_dye",
            "gray_dye",
            "pink_dye",
            "lime_dye",
            "dandelion_yellow",
            "light_blue_dye",
            "magenta_dye",
            "orange_dye",
            "bone_meal"
    };

    public static ItemStack createItemStack(String materialName) {
        return createItemStack(materialName, (byte) -1);
    }

    public static ItemStack createItemStack(String materialName, byte materialData) {
        if (materialName == null) return null;

        boolean   materialsFlattened = isAfterMaterialFlattening();
        Material  material           = null;
        ItemStack itemStack          = null;

        if (materialsFlattened) {
            // 1.13
            //            if (isColorable(materialName)) {
            //                materialName = colorize(materialName, materialData);
            //            } else if ("dye".equalsIgnoreCase(materialName)) {
            //                materialName = DYES[materialData];
            //            }
            //
            //            if (materialData >= 0) {
            //                if ("stone".equalsIgnoreCase(materialName) && materialData < STONE_TYPES.length) {
            //                    materialName = STONE_TYPES[materialData];
            //                }
            //            }
        }

        material = Material.matchMaterial(materialName);

        if (material == null) {
            material = Material.matchMaterial("LEGACY_" + materialName);
        }

        if (material != null) {
            //            if (materialsFlattened || materialData < 0) {
            //                itemStack = new ItemStack(material);
            //            } else {
            //                itemStack = new ItemStack(material, 1, (short) materialData);
            //            }

            itemStack = new ItemStack(material, 1, (short) materialData);
        }

        return itemStack;
    }

    public static String colorize(String materialName, byte materialData) {
        if ("stained_hardened_clay".equalsIgnoreCase(materialName) || "stained_clay".equalsIgnoreCase(materialName)) {
            materialName = materialName.replace("stained_hardened_clay", "terracotta");
        }

        return COLORS[materialData] + "_" + materialName;
    }

    public static boolean isColorable(String materialName) {
        for (String colorable : COLORABLES) {
            if (colorable.equalsIgnoreCase(materialName)) {
                return true;
            }
        }

        return false;
    }

    public static String dataToColorName(byte data) {
        return data < COLORS.length ? COLORS[data] : null;
    }

    public static boolean isAfterMaterialFlattening() {
        String serverPackage = Bukkit.getServer().getClass().getPackage().getName();
        String version = serverPackage.substring(serverPackage.lastIndexOf('.') + 1)
                                      .replace("v", "");
        String[] parts = version.split("_");

        if (parts.length >= 2) {
            int major = Integer.parseInt(parts[0]);
            int minor = Integer.parseInt(parts[1]);

            if (major >= 1 && minor >= 13) {
                return true;
            }
        }

        return false;
    }
}
