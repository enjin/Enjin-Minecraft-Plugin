package com.enjin.rpc.mappings.services;

import com.enjin.core.services.Service;
import com.enjin.rpc.EnjinRPC;
import com.enjin.rpc.mappings.mappings.general.RPCData;
import com.google.gson.reflect.TypeToken;
import com.thetransactioncompany.jsonrpc2.JSONRPC2Request;
import com.thetransactioncompany.jsonrpc2.JSONRPC2Response;
import com.thetransactioncompany.jsonrpc2.client.JSONRPC2Session;
import com.thetransactioncompany.jsonrpc2.client.JSONRPC2SessionException;

import java.util.HashMap;
import java.util.Map;

public class PluginService implements Service {
    public RPCData<Boolean> auth(final String authkey, final int port, final boolean save) {
        String method = "Plugin.auth";
        Map<String, Object> parameters = new HashMap<String, Object>() {{
            put("authkey", authkey);
            put("port", port);
            put("save", save);
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

            RPCData<Boolean> data = EnjinRPC.gson.fromJson(response.toJSONString(), new TypeToken<RPCData<Boolean>>() {}.getType());
            return data;
        } catch (JSONRPC2SessionException e) {
            EnjinRPC.debug("Failed Request to " + session.getURL().toString() + ": " + request.toJSONString());
            return null;
        }
    }
}
