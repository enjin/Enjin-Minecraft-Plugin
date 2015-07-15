package com.enjin.rpc.mappings.mappings.general;

import com.thetransactioncompany.jsonrpc2.JSONRPC2Request;
import com.thetransactioncompany.jsonrpc2.JSONRPC2Response;
import lombok.Getter;

public class RPCResult {
    @Getter
    private ResultType type;
    @Getter
    private String message;
    @Getter
    private JSONRPC2Request request;
    @Getter
    private JSONRPC2Response response;

    public RPCResult(ResultType type, String message, JSONRPC2Request request, JSONRPC2Response response) {
        this.type = type;
        this.message = message;
        this.request = request;
        this.response = response;
    }
}
