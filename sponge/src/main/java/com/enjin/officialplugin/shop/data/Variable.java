package com.enjin.officialplugin.shop.data;

import com.google.gson.annotations.SerializedName;
import lombok.Getter;
import lombok.ToString;

import java.util.Map;

@ToString
public class Variable {
    @Getter
    private String name;
    @Getter
    private String type;
    @SerializedName(value = "max_length")
    @Getter
    private int maxLength;
    @SerializedName(value = "min_length")
    @Getter
    private int minLength;
    @SerializedName(value = "max_value")
    @Getter
    private int maxValue;
    @SerializedName(value = "min_value")
    @Getter
    private int minValue;
    @Getter
    private int required;
    @Getter
    private double pricemin;
    @Getter
    private double pricemax;
    @Getter
    private Map<Integer, Option> options;
}
