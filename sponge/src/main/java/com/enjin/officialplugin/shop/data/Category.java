package com.enjin.officialplugin.shop.data;

import lombok.Getter;
import lombok.ToString;

import java.util.List;

@ToString
public class Category {
    @Getter
    private int id;
    @Getter
    private String name;
    @Getter
    private String info;
    @Getter
    private List<Category> categories;
    @Getter
    private List<Item> items;
}
