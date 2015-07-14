package com.enjin.rpc.mappings.mappings.tickets;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@ToString
@EqualsAndHashCode
public class MetaOptions {
    @Getter
    private int bbcode;
    @Getter
    private int lines;
    @Getter
    private int min;
    @Getter
    private int max;
}
