package com.enjin.rpc.mappings.services;

import com.enjin.core.Enjin;
import com.enjin.core.services.Service;
import com.enjin.rpc.EnjinRPC;
import com.enjin.rpc.mappings.mappings.bungeecord.NodeState;
import com.enjin.rpc.mappings.mappings.general.RPCData;
import com.enjin.rpc.mappings.mappings.plugin.Status;
import com.enjin.rpc.mappings.mappings.plugin.SyncResponse;
import com.google.gson.reflect.TypeToken;
import com.thetransactioncompany.jsonrpc2.JSONRPC2Request;
import com.thetransactioncompany.jsonrpc2.JSONRPC2Response;
import com.thetransactioncompany.jsonrpc2.client.JSONRPC2Session;
import com.thetransactioncompany.jsonrpc2.client.JSONRPC2SessionException;

import java.util.HashMap;
import java.util.Map;

public class BungeeCordService implements Service {
    public RPCData<SyncResponse> get(final Status status, final Map<String, NodeState> servers) {
        String method = "Bungeecord.get";

        Map<String, Object> parameters = new HashMap<String, Object>() {{
            put("authkey", Enjin.getConfiguration().getApiUrl());
            put("status", EnjinRPC.gson.fromJson(EnjinRPC.gson.toJson(status), Object.class));
            put("servers", EnjinRPC.gson.fromJson(EnjinRPC.gson.toJson(servers, new TypeToken<HashMap<String, NodeState>>(){}.getType()), Object.class));
        }};

        int id = EnjinRPC.getNextRequestId();

        JSONRPC2Session session = null;
        JSONRPC2Request request = null;
        JSONRPC2Response response = null;

        try {
            session = EnjinRPC.getSession("minecraft.php");
            request = new JSONRPC2Request(method, parameters, id);
            response = session.send(request);

            EnjinRPC.debug("JSONRPC2 Request: " + request.toJSONString());
            EnjinRPC.debug("JSONRPC2 Response: " + response.toJSONString());

            RPCData<SyncResponse> data = EnjinRPC.gson.fromJson(response.toJSONString(), new TypeToken<RPCData<SyncResponse>>() {}.getType());
            data.setRequest(request);
            data.setResponse(response);
            return data;
        } catch (JSONRPC2SessionException e) {
            EnjinRPC.debug(e.getMessage());
            EnjinRPC.debug("Failed Request to " + session.getURL().toString() + ": " + request.toJSONString());
            return null;
        }
    }
}
