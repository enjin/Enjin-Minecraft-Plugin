package com.enjin.officialplugin.shop;

import java.util.ArrayList;

import org.bukkit.Material;

import com.enjin.officialplugin.shop.ServerShop.Type;

public class ShopCategory extends AbstractShopSuperclass implements ShopItemAdder {

    String id = "";
    String name = "";
    String info = "";
    ServerShop.Type type = Type.Item;
    ShopItemAdder parentcategory = null;

    Material material = Material.CHEST;
    short matdamage = 0;

    ArrayList<AbstractShopSuperclass> items = new ArrayList<AbstractShopSuperclass>();

    ArrayList<ArrayList<String>> pages = null;

    public ShopCategory(String name, String id) {
        this.name = name;
        this.id = id;
    }

    /**
     * Adding the item.
     *
     * @param item The item or category to be added.
     * @throws ItemTypeNotSupported if the container type doesn't support the item being added.
     */
    public void addItem(AbstractShopSuperclass item) throws ItemTypeNotSupported {
        if ((type == Type.Item && item instanceof ShopItem) ||
                (type == Type.Category && item instanceof ShopCategory)) {
            items.add(item);
        } else {
            String itemtype = "an unknown type";
            if (item instanceof ShopItem) {
                itemtype = "an item";
            } else if (item instanceof ShopCategory) {
                itemtype = "a category";
            }
            throw new ItemTypeNotSupported("Got passed " + itemtype + " was expecting a " + type.toString());
        }
    }

    public ArrayList<AbstractShopSuperclass> getItems() {
        return items;
    }

    public AbstractShopSuperclass getItem(int i) {
        try {
            return items.get(i);
        } catch (Exception e) {
            return null;
        }
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getInfo() {
        return info;
    }

    public void setInfo(String info) {
        this.info = info;
    }

    public ServerShop.Type getType() {
        return type;
    }

    public void setType(ServerShop.Type type) {
        this.type = type;
    }

    @Override
    public ShopItemAdder getParentCategory() {
        return parentcategory;
    }

    @Override
    public void setParentCategory(ShopItemAdder cat) {
        parentcategory = cat;
    }

    @Override
    public void setPages(ArrayList<ArrayList<String>> pages) {
        this.pages = pages;
    }

    @Override
    public ArrayList<ArrayList<String>> getPages() {
        return pages;
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
