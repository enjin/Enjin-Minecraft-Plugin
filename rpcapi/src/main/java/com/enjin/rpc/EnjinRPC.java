package com.enjin.rpc;

import com.enjin.core.EnjinServices;
import com.enjin.rpc.mappings.services.TicketsService;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.thetransactioncompany.jsonrpc2.client.JSONRPC2Session;
import com.thetransactioncompany.jsonrpc2.client.JSONRPC2SessionOptions;

import java.net.MalformedURLException;
import java.net.URL;

public class EnjinRPC {
    public static Gson gson = new GsonBuilder()
            .create();

    private static final int READ_TIMEOUT = 15000;
    private static final int CONNECT_TIMEOUT = 15000;

    private static final String API_URL = "https://api.enjin.com/api/v1/api.php/";

    static {
        EnjinServices.registerServices(TicketsService.class);
    }

    private static URL getApiUrl() {
        try {
            return new URL(API_URL);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        return null;
    }

    private static JSONRPC2SessionOptions getOptions() {
        JSONRPC2SessionOptions options = new JSONRPC2SessionOptions();
        options.setReadTimeout(READ_TIMEOUT);
        options.setConnectTimeout(CONNECT_TIMEOUT);
        return options;
    }

    public static JSONRPC2Session getSession() {
        JSONRPC2Session session = new JSONRPC2Session(getApiUrl());
        session.setOptions(getOptions());
        return session;
    }
}
