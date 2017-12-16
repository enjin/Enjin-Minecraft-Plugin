package com.enjin.rpc.mappings.services;

import com.enjin.core.Enjin;
import com.enjin.core.services.Service;
import com.enjin.rpc.EnjinRPC;
import com.enjin.rpc.mappings.adapters.BooleanAdapter;
import com.enjin.rpc.mappings.adapters.ByteAdapter;
import com.enjin.rpc.mappings.adapters.DoubleAdapter;
import com.enjin.rpc.mappings.adapters.FloatAdapter;
import com.enjin.rpc.mappings.adapters.IntegerAdapter;
import com.enjin.rpc.mappings.adapters.LongAdapter;
import com.enjin.rpc.mappings.adapters.ShortAdapter;
import com.enjin.rpc.mappings.deserializers.AuthDeserializer;
import com.enjin.rpc.mappings.deserializers.InstructionDeserializer;
import com.enjin.rpc.mappings.mappings.general.RPCData;
import com.enjin.rpc.mappings.mappings.plugin.*;
import com.google.common.base.Optional;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.thetransactioncompany.jsonrpc2.JSONRPC2Request;
import com.thetransactioncompany.jsonrpc2.JSONRPC2Response;
import com.thetransactioncompany.jsonrpc2.client.JSONRPC2Session;
import com.thetransactioncompany.jsonrpc2.client.JSONRPC2SessionException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class PluginService implements Service {
    private static Gson gson = new GsonBuilder()
            .registerTypeAdapter(Boolean.class, new BooleanAdapter())
            .registerTypeAdapter(Byte.class, new ByteAdapter())
            .registerTypeAdapter(Short.class, new ShortAdapter())
            .registerTypeAdapter(Integer.class, new IntegerAdapter())
            .registerTypeAdapter(Long.class, new LongAdapter())
            .registerTypeAdapter(Float.class, new FloatAdapter())
            .registerTypeAdapter(Double.class, new DoubleAdapter())
            .registerTypeAdapter(Instruction.class, new InstructionDeserializer())
            .registerTypeAdapter(Auth.class, new AuthDeserializer())
            .create();

    public RPCData<Auth> auth(final Optional<String> authKey, final Integer port, final boolean save, final boolean fetchServerId) {
        String method = "Plugin.auth";
        Map<String, Object> parameters = new HashMap<String, Object>() {{
            put("authkey", authKey.isPresent() ? authKey.get() : Enjin.getConfiguration().getAuthKey());
            put("port", port);
            put("save", save);
            put("fetch_server_id", fetchServerId);
        }};

        Integer id = EnjinRPC.getNextRequestId();

        JSONRPC2Session session = null;
        JSONRPC2Request request = null;
        JSONRPC2Response response = null;

        try {
            session = EnjinRPC.getSession("minecraft.php");
            request = new JSONRPC2Request(method, parameters, id);
            response = session.send(request);

            Enjin.getLogger().debug("JSONRPC2 Request: " + request.toJSONString());
            Enjin.getLogger().debug("JSONRPC2 Response: " + response.toJSONString());

            RPCData<Auth> data = gson.fromJson(response.toJSONString(), new TypeToken<RPCData<Auth>>() {
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

    public RPCData<SyncResponse> sync(final Map<String, Object> status) {
        String method = "Plugin.sync";
        Map<String, Object> parameters = new HashMap<String, Object>() {{
            put("authkey", Enjin.getConfiguration().getAuthKey());
            put("status", status);
        }};

        Integer id = EnjinRPC.getNextRequestId();

        JSONRPC2Session session = null;
        JSONRPC2Request request = null;
        JSONRPC2Response response = null;

        try {
            session = EnjinRPC.getSession("minecraft.php");
            request = new JSONRPC2Request(method, parameters, id);
            response = session.send(request);

            Enjin.getLogger().debug("JSONRPC2 Request: " + request.toJSONString());
            Enjin.getLogger().debug("JSONRPC2 Response: " + response.toJSONString());

            RPCData<SyncResponse> data = PluginService.gson.fromJson(response.toJSONString(), new TypeToken<RPCData<SyncResponse>>() {
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

    public RPCData<List<TagData>> getTags(final String player) {
        String method = "Plugin.getTags";
        Map<String, Object> parameters = new HashMap<String, Object>() {{
            put("authkey", Enjin.getConfiguration().getAuthKey());
            put("player", player);
        }};

        Integer id = EnjinRPC.getNextRequestId();

        JSONRPC2Session session = null;
        JSONRPC2Request request = null;
        JSONRPC2Response response = null;

        try {
            session = EnjinRPC.getSession("minecraft.php");
            request = new JSONRPC2Request(method, parameters, id);
            response = session.send(request);

            Enjin.getLogger().debug("JSONRPC2 Request: " + request.toJSONString());
            Enjin.getLogger().debug("JSONRPC2 Response: " + response.toJSONString());

            RPCData<List<TagData>> data = EnjinRPC.gson.fromJson(response.toJSONString(), new TypeToken<RPCData<ArrayList<TagData>>>() {
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

    public RPCData<Boolean> setRank(final String player, final String group, final String world) {
        String method = "Plugin.setRank";
        Map<String, Object> parameters = new HashMap<String, Object>() {{
            put("authkey", Enjin.getConfiguration().getAuthKey());
            put("player", player);
            put("group", group);
            put("world", world);
        }};

        Integer id = EnjinRPC.getNextRequestId();

        JSONRPC2Session session = null;
        JSONRPC2Request request = null;
        JSONRPC2Response response = null;

        try {
            session = EnjinRPC.getSession("minecraft.php");
            request = new JSONRPC2Request(method, parameters, id);
            response = session.send(request);

            Enjin.getLogger().debug("JSONRPC2 Request: " + request.toJSONString());
            Enjin.getLogger().debug("JSONRPC2 Response: " + response.toJSONString());

            RPCData<Boolean> data = EnjinRPC.gson.fromJson(response.toJSONString(), new TypeToken<RPCData<Boolean>>() {
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

    public RPCData<Boolean> removeRank(final String player, final String group, final String world) {
        String method = "Plugin.removeRank";
        Map<String, Object> parameters = new HashMap<String, Object>() {{
            put("authkey", Enjin.getConfiguration().getAuthKey());
            put("player", player);
            put("group", group);
            put("world", world);
        }};

        Integer id = EnjinRPC.getNextRequestId();

        JSONRPC2Session session = null;
        JSONRPC2Request request = null;
        JSONRPC2Response response = null;

        try {
            session = EnjinRPC.getSession("minecraft.php");
            request = new JSONRPC2Request(method, parameters, id);
            response = session.send(request);

            Enjin.getLogger().debug("JSONRPC2 Request: " + request.toJSONString());
            Enjin.getLogger().debug("JSONRPC2 Response: " + response.toJSONString());

            RPCData<Boolean> data = EnjinRPC.gson.fromJson(response.toJSONString(), new TypeToken<RPCData<Boolean>>() {
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

    public RPCData<Stats> getStats(Optional<List<Integer>> items) {
        String method = "Plugin.getStats";
        Map<String, Object> parameters = new HashMap<String, Object>() {{
            put("authkey", Enjin.getConfiguration().getAuthKey());
        }};

        if (items.isPresent()) {
            parameters.put("items", items.get());
        }

        Integer id = EnjinRPC.getNextRequestId();

        JSONRPC2Session session = null;
        JSONRPC2Request request = null;
        JSONRPC2Response response = null;

        try {
            session = EnjinRPC.getSession("minecraft.php");
            request = new JSONRPC2Request(method, parameters, id);
            response = session.send(request);

            Enjin.getLogger().debug("JSONRPC2 Request: " + request.toJSONString());
            Enjin.getLogger().debug("JSONRPC2 Response: " + response.toJSONString());

            RPCData<Stats> data = EnjinRPC.gson.fromJson(response.toJSONString(), new TypeToken<RPCData<Stats>>() {
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
