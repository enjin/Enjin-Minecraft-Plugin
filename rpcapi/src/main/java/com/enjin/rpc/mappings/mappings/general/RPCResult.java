package com.enjin.rpc.mappings.mappings.general;

import lombok.Getter;

public class RPCResult {
    @Getter
    private ResultType type;
    @Getter
    private String message;

    public RPCResult(ResultType type, String message) {
        this.type = type;
        this.message = message;
    }
}
