package com.enjin.rpc;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.thetransactioncompany.jsonrpc2.client.JSONRPC2Session;
import com.thetransactioncompany.jsonrpc2.client.JSONRPC2SessionOptions;
import lombok.Getter;
import lombok.Setter;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Logger;

public class EnjinRPC {
    public static Gson gson = new GsonBuilder()
            .create();

    private static final int READ_TIMEOUT = 15000;
    private static final int CONNECT_TIMEOUT = 15000;

    @Getter @Setter
    private static String apiUrl = "https://api.enjin.com/api/v1/api.php/";
    @Setter
    private static Logger logger;
    @Setter
    private static boolean debug;

    private static URL getUrl() {
        try {
            return new URL(apiUrl);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        return null;
    }

    public static void debug(String s) {
        if (logger == null) {
            return;
        }

        if (debug) {
            logger.info("Enjin Debug: " + (s == null ? "null" : s));
        }
    }

    private static JSONRPC2SessionOptions getOptions() {
        JSONRPC2SessionOptions options = new JSONRPC2SessionOptions();
        options.setReadTimeout(READ_TIMEOUT);
        options.setConnectTimeout(CONNECT_TIMEOUT);
        return options;
    }

    public static JSONRPC2Session getSession() {
        JSONRPC2Session session = new JSONRPC2Session(getUrl());
        session.setOptions(getOptions());
        return session;
    }
}
