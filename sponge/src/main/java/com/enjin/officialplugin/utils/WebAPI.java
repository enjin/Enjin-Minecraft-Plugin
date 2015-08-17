package com.enjin.officialplugin.utils;

import com.enjin.officialplugin.EnjinMinecraftPlugin;
import com.enjin.officialplugin.utils.packet.PacketUtilities;
import lombok.Getter;

import javax.net.ssl.SSLHandshakeException;
import java.io.BufferedInputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;

public class WebAPI {
    @Getter
    static private String apiUrl = "://api.enjin.com/api/";

    /**
     * @param urls
     * @param queryValues
     * @return 0 = Invalid key, 1 = OK, 2 = Exception encountered.
     * @throws MalformedURLException
     */
    public static int sendAPIQuery(String urls, String... queryValues) throws MalformedURLException {
        EnjinMinecraftPlugin plugin = EnjinMinecraftPlugin.getInstance();
        URL url = new URL((plugin.getConfig().isHttps() ? "https" : "http") + apiUrl + urls);
        StringBuilder query = new StringBuilder();

        try {
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");
            con.setReadTimeout(3000);
            con.setConnectTimeout(3000);
            con.setDoOutput(true);
            con.setDoInput(true);

            for (String val : queryValues) {
                query.append('&');
                query.append(val);
            }

            if (queryValues.length > 0) {
                query.deleteCharAt(0); //remove first &
            }

            con.setRequestProperty("Content-length", String.valueOf(query.length()));
            con.getOutputStream().write(query.toString().getBytes());
            String read = PacketUtilities.readString(new BufferedInputStream(con.getInputStream()));

            if (read.charAt(0) == '1') {
                return 1;
            }
        } catch (SSLHandshakeException e) {
            plugin.getLogger().warn("SSLHandshakeException, The plugin will use http without SSL. This may be less secure.");
            plugin.getConfig().setHttps(false);
            return sendAPIQuery(urls, queryValues);
        } catch (SocketTimeoutException e) {
            plugin.getLogger().warn("Timeout, the enjin server didn't respond within the required time. Please be patient and report this bug to enjin.");
            return 2;
        } catch (Throwable t) {
            plugin.getLogger().warn("Failed to send query to enjin server! " + t.getClass().getName() + ". Data: " + url + "?" + query.toString());
            return 2;
        }

        return 0;
    }
}
