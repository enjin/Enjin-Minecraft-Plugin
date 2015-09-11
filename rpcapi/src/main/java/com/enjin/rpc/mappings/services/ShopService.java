package com.enjin.rpc.mappings.services;

import com.enjin.core.services.Service;
import com.enjin.rpc.EnjinRPC;
import com.enjin.rpc.mappings.mappings.general.RPCData;
import com.enjin.rpc.mappings.mappings.shop.FilteredItem;
import com.enjin.rpc.mappings.mappings.shop.Purchase;
import com.enjin.rpc.mappings.mappings.shop.Shop;
import com.google.gson.reflect.TypeToken;
import com.thetransactioncompany.jsonrpc2.JSONRPC2Request;
import com.thetransactioncompany.jsonrpc2.JSONRPC2Response;
import com.thetransactioncompany.jsonrpc2.client.JSONRPC2Session;
import com.thetransactioncompany.jsonrpc2.client.JSONRPC2SessionException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ShopService implements Service {
    public RPCData<List<Shop>> get(final String authkey, final String player) {
        String method = "Shop.get";
        Map<String, Object> parameters = new HashMap<String, Object>() {{
            put("authkey", authkey);
            put("player", player);
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

            RPCData<List<Shop>> data = EnjinRPC.gson.fromJson(response.toJSONString(), new TypeToken<RPCData<ArrayList<Shop>>>() {}.getType());
            return data;
        } catch (JSONRPC2SessionException e) {
            EnjinRPC.debug("Failed Request to " + session.getURL().toString() + ": " + request.toJSONString());
            return null;
        }
    }

    public RPCData<List<Purchase>> getPurchases(final String authkey, final String player, final boolean commands) {
        String method = "Shop.getPurchases";
        Map<String, Object> parameters = new HashMap<String, Object>() {{
            put("authkey", authkey);
            put("player", player);
            put("commands", commands);
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

            RPCData<List<Purchase>> data = EnjinRPC.gson.fromJson(response.toJSONString(), new TypeToken<RPCData<ArrayList<Purchase>>>() {}.getType());
            return data;
        } catch (JSONRPC2SessionException e) {
            EnjinRPC.debug("Failed Request to " + session.getURL().toString() + ": " + request.toJSONString());
            return null;
        }
    }

    public RPCData<List<FilteredItem>> getItems(final String authkey, final String player) {
        String method = "Shop.getItems";
        Map<String, Object> parameters = new HashMap<String, Object>() {{
            put("authkey", authkey);
            put("player", player);
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

            RPCData<List<FilteredItem>> data = EnjinRPC.gson.fromJson(response.toJSONString(), new TypeToken<RPCData<ArrayList<FilteredItem>>>() {}.getType());
            return data;
        } catch (JSONRPC2SessionException e) {
            EnjinRPC.debug("Failed Request to " + session.getURL().toString() + ": " + request.toJSONString());
            return null;
        }
    }
}
