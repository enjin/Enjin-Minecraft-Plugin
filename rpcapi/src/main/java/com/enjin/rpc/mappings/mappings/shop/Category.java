package com.enjin.rpc.mappings.mappings.shop;

import lombok.Getter;

import java.util.List;

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
