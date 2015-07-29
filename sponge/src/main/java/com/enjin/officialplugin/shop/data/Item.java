package com.enjin.officialplugin.shop.data;

import com.google.gson.annotations.SerializedName;
import lombok.Getter;
import lombok.ToString;

import java.util.Map;

@ToString
public class Item {
    @Getter
    private int id;
    @Getter
    private String name;
    @Getter
    private String info;
    @SerializedName(value = "icon_item")
    @Getter
    private String iconItem;
    @SerializedName(value = "icon_damage")
    @Getter
    private int iconDamage;
    @Getter
    private double price;
    @Getter
    private Map<Integer, Variable> variables;
}
