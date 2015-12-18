package com.enjin.rpc.mappings.mappings.tickets;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@ToString
@EqualsAndHashCode
public class MetaOptions {
    @Getter
    private Integer bbcode;
    @Getter
    private Integer lines;
    @Getter
    private Integer min;
    @Getter
    private Integer max;
}
