package com.enjin.rpc.mappings.mappings.shop;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@ToString
@EqualsAndHashCode
public class Option {
    @Getter
    private String  name;
    @Getter
    private String  value;
    @Getter
    private Double  price;
    @Getter
    private Integer points;
}
