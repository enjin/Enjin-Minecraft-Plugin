package com.enjin.rpc.mappings.mappings.shop;

import lombok.Getter;

import java.util.Map;

public class Item {
    @Getter
    private int id;
    @Getter
    private String name;
    @Getter
    private String info;
    @Getter
    private Map<Integer, Variable> variables;
}
