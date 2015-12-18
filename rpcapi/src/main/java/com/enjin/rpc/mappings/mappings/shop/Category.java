package com.enjin.rpc.mappings.mappings.shop;

import com.google.gson.annotations.SerializedName;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.util.List;

@ToString
@EqualsAndHashCode
public class Category {
    @Getter
    private Integer id;
    @Getter
    private String name;
    @Getter
    private String info;
    @Getter
    private List<Category> categories;
    @Getter
    private List<Item> items;
    @Getter
    @SerializedName(value = "icon_item")
    private String iconItem;
    @Getter
    @SerializedName(value = "icon_damage")
    private byte iconDamage;
}
