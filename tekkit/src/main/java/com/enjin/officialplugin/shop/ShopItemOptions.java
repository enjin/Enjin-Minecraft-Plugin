package com.enjin.officialplugin.shop;

public class ShopItemOptions {

    String name = "";
    String minprice = "";
    String maxprice = "";
    String minpoints = "";
    String maxpoints = "";

    public ShopItemOptions(String name, String pricemin, String pricemax, String minpoints, String maxpoints) {
        this.name = name;
        minprice = pricemin;
        maxprice = pricemax;
        this.minpoints = minpoints;
        this.maxpoints = maxpoints;
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

    public String getMinPoints() {
        return minpoints;
    }

    public String getMaxPoints() {
        return maxpoints;
    }
}
