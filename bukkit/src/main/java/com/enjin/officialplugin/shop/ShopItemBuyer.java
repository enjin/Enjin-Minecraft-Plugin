package com.enjin.officialplugin.shop;

import java.util.ArrayList;

public class ShopItemBuyer {

    private ShopItem item;
    int option = 0;
    int totalpoints = 0;
    ArrayList<String> options = new ArrayList<String>();

    public ShopItemBuyer(ShopItem item) {
        this.item = item;
    }

    public void addOption(String option) {
        options.add(option);
    }

    public ArrayList<String> getOptions() {
        return options;
    }

    public ShopItemOptions getNextItemOption() {
        option++;
        if (item.getOptions().size() > option) {
            return item.getOption(option);
        } else {
            return null;
        }
    }

    public ShopItem getItem() {
        return item;
    }

    public void addPoints(String points) {
        try {
            if (points.contains(".")) {
                String[] split = points.split(".");
                int ipoints = Integer.parseInt(split[0]);
                totalpoints += ipoints;
            } else {
                int ipoints = Integer.parseInt(points.trim());
                totalpoints += ipoints;
            }
        } catch (NumberFormatException e) {

        }
    }

    public void addPoints(int points) {
        totalpoints += points;
    }

    public ShopItemOptions getCurrentItemOption() {
        if (item.getOptions().size() > option) {
            return item.getOption(option);
        } else {
            return null;
        }
    }
}