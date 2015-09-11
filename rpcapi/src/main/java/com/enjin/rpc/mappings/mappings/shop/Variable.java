package com.enjin.rpc.mappings.mappings.shop;

import com.google.gson.annotations.SerializedName;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.util.Map;

@ToString
@EqualsAndHashCode
public class Variable {
    @Getter
    private String name;
    @Getter
    private String type;
    @Getter
    @SerializedName(value = "max_length")
    private int maxLength;
    @SerializedName(value = "min_length")
    private int minLength;
    @SerializedName(value = "max_value")
    private int maxValue;
    @SerializedName(value = "min_value")
    private int minValue;
    @Getter
    private boolean required;
    @Getter
    private Map<Integer, Option> options;
    @Getter
    @SerializedName(value = "pricemin")
    private double minPrice;
    @Getter
    @SerializedName(value = "pricemax")
    private double maxPrice;
}
