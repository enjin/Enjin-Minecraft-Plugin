package com.enjin.rpc.mappings.services;

import com.enjin.core.Enjin;
import com.enjin.core.services.Service;
import com.enjin.rpc.EnjinRPC;
import com.enjin.rpc.mappings.mappings.general.RPCData;
import com.google.gson.reflect.TypeToken;
import com.thetransactioncompany.jsonrpc2.JSONRPC2Request;
import com.thetransactioncompany.jsonrpc2.JSONRPC2Response;
import com.thetransactioncompany.jsonrpc2.client.JSONRPC2Session;
import com.thetransactioncompany.jsonrpc2.client.JSONRPC2SessionException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class VoteService implements Service {
    public RPCData<String> get(final Map<String, List<Object[]>> votes) {
        String method = "Votifier.get";
        Map<String, Object> parameters = new HashMap<String, Object>() {{
            put("authkey", Enjin.getConfiguration().getAuthKey());
            put("votifier", votes);
        }};

        Integer id = EnjinRPC.getNextRequestId();

        JSONRPC2Session  session  = null;
        JSONRPC2Request  request  = null;
        JSONRPC2Response response = null;

        try {
            session = EnjinRPC.getSession("minecraft.php");
            request = new JSONRPC2Request(method, parameters, id);
            response = session.send(request);

            Enjin.getLogger().debug("JSONRPC2 Request: " + request.toJSONString());
            Enjin.getLogger().debug("JSONRPC2 Response: " + response.toJSONString());

            RPCData<String> data = EnjinRPC.gson.fromJson(response.toJSONString(), new TypeToken<RPCData<String>>() {
            }.getType());
            data.setRequest(request);
            data.setResponse(response);
            return data;
        } catch (JSONRPC2SessionException e) {
            Enjin.getLogger().debug(e.getMessage());
            Enjin.getLogger().debug("Failed Request to " + session.getURL().toString() + ": " + request.toJSONString());
            return null;
        }
    }
}
