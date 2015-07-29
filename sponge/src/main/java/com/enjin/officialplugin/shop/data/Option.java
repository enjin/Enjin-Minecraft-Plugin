package com.enjin.officialplugin.shop.data;

import lombok.Getter;
import lombok.ToString;

@ToString
public class Option {
    @Getter
    private String name;
    @Getter
    private String value;
    @Getter
    private double price;
    @Getter
    private int points;
}
