package com.enjin.rpc.mappings.services;

import com.enjin.core.Enjin;
import com.enjin.core.services.Service;
import com.enjin.rpc.EnjinRPC;
import com.enjin.rpc.mappings.mappings.general.RPCData;
import com.enjin.rpc.mappings.mappings.shop.FilteredItem;
import com.enjin.rpc.mappings.mappings.shop.Purchase;
import com.enjin.rpc.mappings.mappings.shop.Shop;
import com.google.common.base.Optional;
import com.google.gson.reflect.TypeToken;
import com.thetransactioncompany.jsonrpc2.JSONRPC2Request;
import com.thetransactioncompany.jsonrpc2.JSONRPC2Response;
import com.thetransactioncompany.jsonrpc2.client.JSONRPC2Session;
import com.thetransactioncompany.jsonrpc2.client.JSONRPC2SessionException;

import java.util.*;

public class ShopService implements Service {
    public RPCData<List<Shop>> get(final String player) {
        String method = "Shop.get";
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

            RPCData<List<Shop>> data = EnjinRPC.gson.fromJson(response.toJSONString(), new TypeToken<RPCData<ArrayList<Shop>>>() {}.getType());
            data.setRequest(request);
            data.setResponse(response);
            return data;
        } catch (JSONRPC2SessionException e) {
            Enjin.getLogger().debug(e.getMessage());
            Enjin.getLogger().debug("Failed Request to " + session.getURL().toString() + ": " + request.toJSONString());
            return null;
        }
    }

    public RPCData<List<Purchase>> getPurchases(final String player, final boolean commands) {
        String method = "Shop.getPurchases";
        Map<String, Object> parameters = new HashMap<String, Object>() {{
            put("authkey", Enjin.getConfiguration().getAuthKey());
            put("player", player);
            put("commands", commands);
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

            RPCData<List<Purchase>> data = EnjinRPC.gson.fromJson(response.toJSONString(), new TypeToken<RPCData<ArrayList<Purchase>>>() {}.getType());
            data.setRequest(request);
            data.setResponse(response);
            return data;
        } catch (JSONRPC2SessionException e) {
            Enjin.getLogger().debug(e.getMessage());
            Enjin.getLogger().debug("Failed Request to " + session.getURL().toString() + ": " + request.toJSONString());
            return null;
        }
    }

    public RPCData<List<FilteredItem>> getItems(final String player) {
        String method = "Shop.getItems";
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

            RPCData<List<FilteredItem>> data = EnjinRPC.gson.fromJson(response.toJSONString(), new TypeToken<RPCData<ArrayList<FilteredItem>>>() {}.getType());
            data.setRequest(request);
            data.setResponse(response);
            return data;
        } catch (JSONRPC2SessionException e) {
            Enjin.getLogger().debug(e.getMessage());
            Enjin.getLogger().debug("Failed Request to " + session.getURL().toString() + ": " + request.toJSONString());
            return null;
        }
    }

    public RPCData<Integer> purchase(final String player, final Integer itemId, final Optional<Map<Integer, String>> variables, final Optional<Integer> customPoints, final Optional<Integer> customPrice, final boolean ignoreMessages) {
        String method = "Shop.purchase";
        Map<String, Object> parameters = new HashMap<String, Object>() {{
            put("authkey", Enjin.getConfiguration().getAuthKey());
            put("player", player);
            put("item_id", itemId);
            put("ignore_messages", ignoreMessages);
        }};

        if (variables.isPresent()) {
            parameters.put("variables", variables.get());
        }

        if (customPoints.isPresent()) {
            parameters.put("custom_points", customPoints.get());
        }

        if (customPrice.isPresent()) {
            parameters.put("custom_price", customPrice.get());
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

            RPCData<Integer> data = EnjinRPC.gson.fromJson(response.toJSONString(), new TypeToken<RPCData<Integer>>() {}.getType());
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
