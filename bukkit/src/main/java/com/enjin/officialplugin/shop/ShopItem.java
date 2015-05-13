package com.enjin.officialplugin.shop;

import java.util.ArrayList;

import org.bukkit.Material;

public class ShopItem extends AbstractShopSuperclass {

    String id = "";
    String name = "";
    String price = "";
    String info = "";
    String points = "";

    Material material = Material.PAPER;
    short matdamage = 0;

    ArrayList<ShopItemOptions> options = new ArrayList<ShopItemOptions>();

    public ShopItem(String name, String id, String price, String info, String points) {
        this.name = name;
        this.id = id;
        this.price = price;
        this.info = info;
        this.points = points;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getPrice() {
        return price;
    }

    public String getInfo() {
        return info;
    }

    public String getPoints() {
        return points;
    }

    public void addOption(ShopItemOptions option) {
        options.add(option);
    }

    public ArrayList<ShopItemOptions> getOptions() {
        return options;
    }

    public ShopItemOptions getOption(int i) {
        try {
            return options.get(i);
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public Material getMaterial() {
        return material;
    }

    @Override
    public short getMaterialDamage() {
        return matdamage;
    }

    @Override
    public void setMaterial(Material mat) {
        material = mat;
    }

    @Override
    public void setMaterialDamage(short dmg) {
        matdamage = dmg;
    }
}
