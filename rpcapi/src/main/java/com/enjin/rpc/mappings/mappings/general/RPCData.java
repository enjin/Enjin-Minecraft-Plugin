package com.enjin.rpc.mappings.mappings.general;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@ToString
@EqualsAndHashCode
public class RPCData<T> {
    @Getter
    private int id;
    @Getter
    private T result;
    @Getter
    private RPCError error;
}
