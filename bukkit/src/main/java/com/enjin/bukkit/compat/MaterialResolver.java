package com.enjin.bukkit.compat;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class MaterialResolver {

    public static ItemStack createItemStack(String materialName, byte materialData) {
        if (materialName == null) return null;

        ItemStack itemStack = null;
        Material material = Material.matchMaterial(materialName);

        if (material == null) {
            material = Material.matchMaterial("LEGACY_" + materialName);
        }

        if (material != null) {
            itemStack = new ItemStack(material, 1, (short) materialData);
        }

        return itemStack;
    }

}
