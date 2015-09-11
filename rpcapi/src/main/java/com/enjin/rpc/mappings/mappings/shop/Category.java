package com.enjin.rpc.mappings.mappings.shop;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.util.List;

@ToString
@EqualsAndHashCode
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
