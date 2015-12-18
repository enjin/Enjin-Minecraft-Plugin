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
    private Integer id;
    @Getter
    private String name;
    @Getter
    private String info;
    @Getter
    @SerializedName(value = "buyurl")
    private String buyUrl;
    @Getter
    private String currency;
    @Getter
    @SerializedName(value = "ingame_purchase_points_enabled")
    private Boolean pointsEnabled;
    @Getter
    @SerializedName(value = "current_points")
    private Integer currentPoints;
    @Getter
    @SerializedName(value = "simpleitems")
    private Boolean simpleItems;
    @Getter
    @SerializedName("colortext")
    private String colorText;
    @Getter
    @SerializedName("colortitle")
    private String colorTitle;
    @Getter
    @SerializedName("colorid")
    private String colorId;
    @Getter
    @SerializedName("colorname")
    private String colorName;
    @Getter
    @SerializedName("colorprice")
    private String colorPrice;
    @Getter
    @SerializedName("colorbracket")
    private String colorBracket;
    @Getter
    @SerializedName("colorurl")
    private String colorUrl;
    @Getter
    @SerializedName("colorinfo")
    private String colorInfo;
    @Getter
    @SerializedName("colorborder")
    private String colorBorder;
    @Getter
    @SerializedName("colorbottom")
    private String colorBottom;
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
