package com.enjin.rpc.mappings.mappings.shop;

import com.google.gson.annotations.SerializedName;
import lombok.Getter;

import java.util.List;

public class Shop {
    @Getter
    private int id;
    @Getter
    private String name;
    @Getter
    @SerializedName(value = "buyurl")
    private String buyUrl;
    @Getter
    private String currency;
    @Getter
    @SerializedName(value = "ingame_purchase_points_enabled")
    private boolean pointsEnabled;
    @Getter
    @SerializedName(value = "current_points")
    private int currentPoints;
    @Getter
    private List<Category> categories;
}
