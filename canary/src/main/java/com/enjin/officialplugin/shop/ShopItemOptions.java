package com.enjin.officialplugin.shop;

public class ShopItemOptions {

    String name = "";
    String minprice = "";
    String maxprice = "";

    public ShopItemOptions(String name, String pricemin, String pricemax) {
        this.name = name;
        minprice = pricemin;
        maxprice = pricemax;
    }

    public String getName() {
        return name;
    }

    public String getMinPrice() {
        return minprice;
    }

    public String getMaxPrice() {
        return maxprice;
    }
}
