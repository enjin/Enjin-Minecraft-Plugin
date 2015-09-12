package com.enjin.rpc.mappings.mappings.general;

import com.thetransactioncompany.jsonrpc2.JSONRPC2Request;
import com.thetransactioncompany.jsonrpc2.JSONRPC2Response;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
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
    @Getter @Setter
    private transient JSONRPC2Request request;
    @Getter @Setter
    private transient JSONRPC2Response response;
}
