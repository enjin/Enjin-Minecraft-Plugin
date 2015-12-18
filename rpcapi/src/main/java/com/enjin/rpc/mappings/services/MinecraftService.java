package com.enjin.rpc.mappings.services;

import com.enjin.core.Enjin;
import com.enjin.core.services.Service;
import com.enjin.rpc.EnjinRPC;
import com.enjin.rpc.mappings.mappings.general.RPCData;
import com.enjin.rpc.mappings.mappings.minecraft.ServerInfo;
import com.enjin.rpc.mappings.mappings.minecraft.MinecraftPlayerInfo;
import com.google.gson.reflect.TypeToken;
import com.thetransactioncompany.jsonrpc2.JSONRPC2Request;
import com.thetransactioncompany.jsonrpc2.JSONRPC2Response;
import com.thetransactioncompany.jsonrpc2.client.JSONRPC2Session;
import com.thetransactioncompany.jsonrpc2.client.JSONRPC2SessionException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class MinecraftService implements Service {
    public RPCData<List<ServerInfo>> getServers() {
        String method = "Minecraft.getServers";
        Map<String, Object> parameters = new HashMap<String, Object>() {{
            put("authkey", Enjin.getConfiguration().getAuthKey());
        }};

        Integer id = EnjinRPC.getNextRequestId();

        JSONRPC2Session session = null;
        JSONRPC2Request request = null;
        JSONRPC2Response response = null;

        try {
            session = EnjinRPC.getSession("api.php");
            request = new JSONRPC2Request(method, parameters, id);
            response = session.send(request);

            EnjinRPC.debug("JSONRPC2 Request: " + request.toJSONString());
            EnjinRPC.debug("JSONRPC2 Response: " + response.toJSONString());

            RPCData<List<ServerInfo>> data = EnjinRPC.gson.fromJson(response.toJSONString(), new TypeToken<RPCData<List<ServerInfo>>>() {}.getType());
            data.setRequest(request);
            data.setResponse(response);
            return data;
        } catch (JSONRPC2SessionException e) {
            EnjinRPC.debug(e.getMessage());
            EnjinRPC.debug("Failed Request to " + session.getURL().toString() + ": " + request.toJSONString());
            return null;
        }
    }

    public RPCData<List<MinecraftPlayerInfo>> getPlayers(final Integer serverId, final Optional<List<String>> names, final Optional<List<String>> uuids) {
        String method = "Minecraft.getPlayers";
        Map<String, Object> parameters = new HashMap<String, Object>() {{
            put("authkey", Enjin.getConfiguration().getAuthKey());
            put("server_id", serverId);
        }};

        if (names.isPresent()) {
            parameters.put("names", names.get());
        }

        if (uuids.isPresent()) {
            parameters.put("uuids", uuids.get());
        }

        Integer id = EnjinRPC.getNextRequestId();

        JSONRPC2Session session = null;
        JSONRPC2Request request = null;
        JSONRPC2Response response = null;

        try {
            session = EnjinRPC.getSession("api.php");
            request = new JSONRPC2Request(method, parameters, id);
            response = session.send(request);

            EnjinRPC.debug("JSONRPC2 Request: " + request.toJSONString());
            EnjinRPC.debug("JSONRPC2 Response: " + response.toJSONString());

            RPCData<List<MinecraftPlayerInfo>> data = EnjinRPC.gson.fromJson(response.toJSONString(), new TypeToken<RPCData<List<MinecraftPlayerInfo>>>() {}.getType());
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
