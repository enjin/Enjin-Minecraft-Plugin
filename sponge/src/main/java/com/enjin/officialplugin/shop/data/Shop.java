package com.enjin.officialplugin.shop.data;

import com.google.gson.annotations.SerializedName;
import lombok.Getter;
import lombok.ToString;

import java.util.List;

@ToString
public class Shop {
    @Getter
    private int id;
    @Getter
    private String name;
    @Getter
    private String info;
    @Getter
    private String buyurl;
    @Getter
    private String currency;
    @SerializedName(value = "ingame_purchase_points_enabled")
    @Getter
    private int ingamePurchasePointsEnabled;
    @Getter
    private boolean simpleitems;
    @Getter
    private boolean simplecategories;
    @Getter
    private String colortext;
    @Getter
    private String colortitle;
    @Getter
    private String colorid;
    @Getter
    private String colorname;
    @Getter
    private String colorprice;
    @Getter
    private String colorbracket;
    @Getter
    private String colorurl;
    @Getter
    private String colorinfo;
    @Getter
    private String colorborder;
    @Getter
    private String colorbottom;
    @SerializedName(value = "border_v")
    @Getter
    private String borderV;
    @SerializedName(value = "border_h")
    @Getter
    private String borderH;
    @SerializedName(value = "border_c")
    @Getter
    private String borderC;
    @Getter
    private List<Category> categories;
}
