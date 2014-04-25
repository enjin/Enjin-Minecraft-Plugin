package com.enjin.officialplugin.shop;

import org.bukkit.Material;

public abstract class AbstractShopSuperclass {

	abstract public Material getMaterial();
	abstract public void setMaterial(Material mat);
	abstract public short getMaterialDamage();
	abstract public void setMaterialDamage(short dmg);
	
}
