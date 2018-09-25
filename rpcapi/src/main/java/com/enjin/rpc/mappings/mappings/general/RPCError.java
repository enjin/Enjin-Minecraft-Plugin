package com.enjin.rpc.mappings.mappings.general;

import lombok.Getter;

public class RPCError {
    @Getter
    private Integer code;
    @Getter
    private String  message;
}
