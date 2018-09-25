package com.enjin.rpc;

import com.enjin.core.Enjin;
import com.enjin.core.config.EnjinConfig;
import com.enjin.rpc.mappings.adapters.BooleanAdapter;
import com.enjin.rpc.mappings.adapters.ByteAdapter;
import com.enjin.rpc.mappings.adapters.DoubleAdapter;
import com.enjin.rpc.mappings.adapters.FloatAdapter;
import com.enjin.rpc.mappings.adapters.IntegerAdapter;
import com.enjin.rpc.mappings.adapters.LongAdapter;
import com.enjin.rpc.mappings.adapters.ShortAdapter;
import com.enjin.rpc.util.ConnectionUtil;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.thetransactioncompany.jsonrpc2.client.JSONRPC2Session;
import com.thetransactioncompany.jsonrpc2.client.JSONRPC2SessionOptions;

import java.net.MalformedURLException;
import java.net.Proxy;
import java.net.URL;

public class EnjinRPC {
    public static Gson gson = new GsonBuilder()
            .registerTypeAdapter(Boolean.class, new BooleanAdapter())
            .registerTypeAdapter(Byte.class, new ByteAdapter())
            .registerTypeAdapter(Short.class, new ShortAdapter())
            .registerTypeAdapter(Integer.class, new IntegerAdapter())
            .registerTypeAdapter(Long.class, new LongAdapter())
            .registerTypeAdapter(Float.class, new FloatAdapter())
            .registerTypeAdapter(Double.class, new DoubleAdapter())
            .create();

    private static final Integer READ_TIMEOUT    = 15000;
    private static final Integer CONNECT_TIMEOUT = 15000;

    private static Integer nextRequestId = 0;

    public static URL getUrl(String clazz) {
        try {
            EnjinConfig   config  = Enjin.getConfiguration();
            String        apiUrl  = config.getApiUrl();
            StringBuilder builder = new StringBuilder(config.isHttps() ? "https" : "http");

            if (apiUrl.startsWith("https")) {
                apiUrl = apiUrl.replaceFirst("https", "");
            } else if (apiUrl.startsWith("http")) {
                apiUrl = apiUrl.replaceFirst("http", "");
            }

            if (apiUrl.contains("%s")) {
                apiUrl = apiUrl.replace("%s", clazz);
                builder.append(apiUrl);
            } else {
                builder.append(apiUrl);

                if (!apiUrl.endsWith("/")) {
                    builder.append("/");
                }

                builder.append(clazz);
            }

            URL url = new URL(builder.toString());
            Enjin.getLogger().debug("Enjin API URL: " + url.toString());
            return url;
        } catch (MalformedURLException e) {
            Enjin.getLogger().log(e);
        }

        return null;
    }

    private static JSONRPC2SessionOptions getOptions() {
        JSONRPC2SessionOptions options = new JSONRPC2SessionOptions();
        options.setReadTimeout(READ_TIMEOUT);
        options.setConnectTimeout(CONNECT_TIMEOUT);
        options.ignoreVersion(true);
        options.trustAllCerts(true);
        if (ConnectionUtil.isMineshafterPresent()) options.setProxy(Proxy.NO_PROXY);
        return options;
    }

    public static JSONRPC2Session getSession(String clazz) {
        URL url = getUrl(clazz);

        if (url == null) {
            Enjin.getLogger().debug("Api url is null.");
            return null;
        }

        JSONRPC2Session session = new JSONRPC2Session(url);
        session.setOptions(getOptions());
        return session;
    }

    public static int getNextRequestId() {
        return ++nextRequestId;
    }
}
