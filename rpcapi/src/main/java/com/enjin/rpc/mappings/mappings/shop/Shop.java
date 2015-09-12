package com.enjin.rpc.mappings.mappings.shop;

import com.google.gson.annotations.SerializedName;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.util.List;

@ToString
@EqualsAndHashCode
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
    private boolean simpleitems;
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
