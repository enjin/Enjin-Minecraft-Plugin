package com.enjin.officialplugin.shop;

import java.util.ArrayList;

import org.bukkit.Material;

public class ServerShop extends AbstractShopSuperclass implements ShopItemAdder {

    public enum Type {
        Category,
        Item;
    }

    Type containertype = Type.Item;
    String name = "";
    String info = "";
    String buyurl = "";
    String currency = "USD";
    String colortitle = "6";
    String colortext = "f";
    String colorid = "6";
    String colorname = "e";
    String colorprice = "a";
    String colorbracket = "f";
    String colorurl = "f";
    String colorinfo = "7";
    String colorborder = "f";
    String colorbottom = "e";
    String border_v = "| ";
    String border_h = "-";
    String border_c = "+";
    boolean simpleitems = false;
    boolean simplecategories = false;

    Material material = Material.CHEST;
    short matdamage = 0;

    ArrayList<AbstractShopSuperclass> items = new ArrayList<AbstractShopSuperclass>();

    ArrayList<ArrayList<String>> pages = null;

    public ServerShop(String name) {
        this.name = name;
    }

    public ServerShop(Type containertype) {
        this.containertype = containertype;
    }

    public void setType(Type type) {
        containertype = type;
    }

    public Type getType() {
        return containertype;
    }

    public void setSimpleItems(boolean value) {
        simpleitems = value;
    }

    public boolean simpleItemModeDisplay() {
        return simpleitems;
    }

    public String getName() {
        return name;
    }

    /**
     * Adding the item.
     *
     * @param item The item or category to be added.
     * @throws ItemTypeNotSupported if the container type doesn't support the item being added.
     */
    public void addItem(AbstractShopSuperclass item) throws ItemTypeNotSupported {
        if ((containertype == Type.Item && item instanceof ShopItem) ||
                (containertype == Type.Category && item instanceof ShopCategory)) {
            items.add(item);
        } else {
            String itemtype = "an unknown type";
            if (item instanceof ShopItem) {
                itemtype = "an item";
            } else if (item instanceof ShopCategory) {
                itemtype = "a category";
            }
            throw new ItemTypeNotSupported("Got passed " + itemtype + " was expecting a " + containertype.toString());
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

    public String getInfo() {
        return info;
    }

    public void setInfo(String info) {
        this.info = info;
    }

    public String getBuyurl() {
        return buyurl;
    }

    public void setBuyurl(String buyurl) {
        this.buyurl = buyurl;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getColortitle() {
        return colortitle;
    }

    public void setColortitle(String colortitle) {
        this.colortitle = colortitle;
    }

    public String getColortext() {
        return colortext;
    }

    public void setColortext(String colortext) {
        this.colortext = colortext;
    }

    public String getColorid() {
        return colorid;
    }

    public void setColorid(String colorid) {
        this.colorid = colorid;
    }

    public String getColorname() {
        return colorname;
    }

    public void setColorname(String colorname) {
        this.colorname = colorname;
    }

    public String getColorprice() {
        return colorprice;
    }

    public void setColorprice(String colorprice) {
        this.colorprice = colorprice;
    }

    public String getColorbracket() {
        return colorbracket;
    }

    public void setColorbracket(String colorbracket) {
        this.colorbracket = colorbracket;
    }

    public String getColorurl() {
        return colorurl;
    }

    public void setColorurl(String colorurl) {
        this.colorurl = colorurl;
    }

    public String getColorinfo() {
        return colorinfo;
    }

    public void setColorinfo(String colorinfo) {
        this.colorinfo = colorinfo;
    }

    public String getColorborder() {
        return colorborder;
    }

    public void setColorborder(String colorborder) {
        this.colorborder = colorborder;
    }

    public String getColorbottom() {
        return colorbottom;
    }

    public void setColorbottom(String colorbottom) {
        this.colorbottom = colorbottom;
    }

    public String getBorder_v() {
        return border_v;
    }

    public void setBorder_v(String border_v) {
        this.border_v = border_v;
    }

    public String getBorder_h() {
        return border_h;
    }

    public void setBorder_h(String border_h) {
        this.border_h = border_h;
    }

    public String getBorder_c() {
        return border_c;
    }

    public void setBorder_c(String border_c) {
        this.border_c = border_c;
    }

    public boolean simpleCategoryModeDisplay() {
        return simplecategories;
    }

    public void setSimplecategories(boolean simplecategories) {
        this.simplecategories = simplecategories;
    }

    @Override
    public ShopItemAdder getParentCategory() {
        return null;
    }

    @Override
    public void setParentCategory(ShopItemAdder category) {
        //A shop cannot have a parent category. So this is empty.
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
