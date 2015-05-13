package com.enjin.officialplugin.points;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.Proxy;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLEncoder;

import net.md_5.bungee.BungeeCord;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import com.enjin.emp.bungee.EnjinPlugin;

public class PointsAPI {

    public enum Type {
        AddPoints,
        RemovePoints,
        SetPoints;
    }

    /**
     * Use this method to add/remove/set the amount of points for a specific player if you don't care if the method
     * fails or not.
     *
     * @param player The player to add/remove/set points on.
     * @param amount The amount of points to add/remove/set on the player.
     * @param type   Whether we are adding, removing, or setting points.
     */
    public static void modifyPointsToPlayerAsynchronously(String player, int amount, Type type) {
        PointsSyncClass mthread = new PointsSyncClass(player, amount, type);
        BungeeCord.getInstance().getScheduler().runAsync(EnjinPlugin.getInstance(), mthread);
    }

    /**
     * Use this method to add/remove/set the amount of points for a specific player. Please note that since this method
     * opens up a connection to the web it should be run asynchronously in a separate thread to avoid freezing the server.
     *
     * @param player The player to set the points on.
     * @param amount How many points to add/remove/set
     * @param type   What are we doing, adding/removing, or setting?
     * @return The total amount of points for the player after the transaction.
     * @throws PlayerDoesNotExistException     We return this exception if the player doesn't have an account on Enjin.
     * @throws ErrorConnectingToEnjinException An error happened connecting to Enjin.
     * @throws NumberFormatException           This error is thrown when you send a number that's less than 1
     */
    public static int modifyPointsToPlayer(String player, int amount, Type type) throws PlayerDoesNotExistException,
            ErrorConnectingToEnjinException, NumberFormatException {
        try {
            if (amount < 1) {
                throw new NumberFormatException("The amount cannot be negative or 0!");
            }
            EnjinPlugin.debug("Connecting to Enjin for action " + type.toString() + " for " + amount + " points for player " + player);
            URL enjinurl;
            switch (type) {
                case AddPoints:
                    enjinurl = getAddPointsUrl();
                    break;
                case RemovePoints:
                    enjinurl = getRemovePointsUrl();
                    break;
                case SetPoints:
                    enjinurl = getSetPointsUrl();
                    break;
                default:
                    enjinurl = getAddPointsUrl();
                    break;
            }
            HttpURLConnection con;
            // Mineshafter creates a socks proxy, so we can safely bypass it
            // It does not reroute POST requests so we need to go around it
            if (EnjinPlugin.isMineshafterPresent()) {
                con = (HttpURLConnection) enjinurl.openConnection(Proxy.NO_PROXY);
            } else {
                con = (HttpURLConnection) enjinurl.openConnection();
            }
            con.setRequestMethod("POST");
            con.setReadTimeout(15000);
            con.setConnectTimeout(15000);
            con.setDoInput(true);
            con.setDoOutput(true);
            con.setRequestProperty("User-Agent", "Mozilla/4.0");
            con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

            StringBuilder builder = new StringBuilder();
            builder.append("authkey=" + encode(EnjinPlugin.hash));
            builder.append("&points=" + amount);
            builder.append("&player=" + player);
            con.setRequestProperty("Content-Length", String.valueOf(builder.length()));
            EnjinPlugin.debug("Sending content: \n" + builder.toString());
            con.getOutputStream().write(builder.toString().getBytes());
            //System.out.println("Getting input stream...");
            InputStream in = con.getInputStream();
            //System.out.println("Handling input stream...");
            String json = parseInput(in);

            EnjinPlugin.debug("Content of points query for type " + type.toString() + ":\n" + json);
            //Let's parse the json
            JSONParser parser = new JSONParser();

            JSONObject array = (JSONObject) parser.parse(json);
            String success = array.get("success").toString();
            if (success.equalsIgnoreCase("true")) {
                String spoints = array.get("points").toString();
                int points = Integer.parseInt(spoints);
                return points;
            } else {
                String error = array.get("error").toString();
                throw new PlayerDoesNotExistException(error);
            }
        } catch (SocketTimeoutException e) {
            throw new ErrorConnectingToEnjinException("Unable to connect to enjin to add points.");
        } catch (UnsupportedEncodingException e) {
            throw new ErrorConnectingToEnjinException("Unable to connect to enjin to add points.");
        } catch (PlayerDoesNotExistException e) {
            throw e;
        } catch (NumberFormatException e) {
            throw e;
        } catch (IOException e) {
            throw new ErrorConnectingToEnjinException("Unable to connect to enjin to add points.");
        } catch (Throwable e) {
            throw new ErrorConnectingToEnjinException("Unable to connect to enjin to add points.");
        }

    }

    /**
     * Gets the amount of points for a specific user. Please note that since this method
     * opens up a connection to the web it should be run asynchronously in a separate thread to avoid freezing the server.
     *
     * @param player The player name to get the points for.
     * @return The amount of points the player has.
     * @throws PlayerDoesNotExistException     We return this exception if the player doesn't have an account on Enjin.
     * @throws ErrorConnectingToEnjinException An error happened connecting to Enjin.
     */
    public static int getPointsForPlayer(String player) throws PlayerDoesNotExistException, ErrorConnectingToEnjinException {
        try {
            EnjinPlugin.debug("Connecting to Enjin to retrieve points balance for player " + player);
            URL enjinurl = getPointsUrl();
            HttpURLConnection con;
            // Mineshafter creates a socks proxy, so we can safely bypass it
            // It does not reroute POST requests so we need to go around it
            if (EnjinPlugin.isMineshafterPresent()) {
                con = (HttpURLConnection) enjinurl.openConnection(Proxy.NO_PROXY);
            } else {
                con = (HttpURLConnection) enjinurl.openConnection();
            }
            con.setRequestMethod("POST");
            con.setReadTimeout(15000);
            con.setConnectTimeout(15000);
            con.setDoInput(true);
            con.setDoOutput(true);
            con.setRequestProperty("User-Agent", "Mozilla/4.0");
            con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

            StringBuilder builder = new StringBuilder();
            builder.append("authkey=" + encode(EnjinPlugin.hash));
            builder.append("&player=" + player);
            con.setRequestProperty("Content-Length", String.valueOf(builder.length()));
            EnjinPlugin.debug("Sending content: \n" + builder.toString());
            con.getOutputStream().write(builder.toString().getBytes());
            //System.out.println("Getting input stream...");
            InputStream in = con.getInputStream();
            //System.out.println("Handling input stream...");
            String json = parseInput(in);

            EnjinPlugin.debug("Content of points query:\n" + json);
            //Let's parse the json
            JSONParser parser = new JSONParser();

            JSONObject array = (JSONObject) parser.parse(json);
            String success = array.get("success").toString();
            if (success.equalsIgnoreCase("true")) {
                String spoints = array.get("points").toString();
                int points = Integer.parseInt(spoints);
                return points;
            } else {
                String error = array.get("error").toString();
                throw new PlayerDoesNotExistException(error);
            }
        } catch (SocketTimeoutException e) {
            throw new ErrorConnectingToEnjinException("Unable to connect to enjin to add points.");
        } catch (UnsupportedEncodingException e) {
            throw new ErrorConnectingToEnjinException("Unable to connect to enjin to add points.");
        } catch (IOException e) {
            throw new ErrorConnectingToEnjinException("Unable to connect to enjin to add points.");
        } catch (PlayerDoesNotExistException e) {
            throw e;
        } catch (Throwable e) {
            throw new ErrorConnectingToEnjinException("Unable to connect to enjin to add points.");
        }

    }

    private static URL getAddPointsUrl() throws Throwable {
        return new URL((EnjinPlugin.usingSSL ? "https" : "http") + EnjinPlugin.apiurl + "add-points");
    }

    private static URL getRemovePointsUrl() throws Throwable {
        return new URL((EnjinPlugin.usingSSL ? "https" : "http") + EnjinPlugin.apiurl + "remove-points");
    }

    private static URL getSetPointsUrl() throws Throwable {
        return new URL((EnjinPlugin.usingSSL ? "https" : "http") + EnjinPlugin.apiurl + "set-points");
    }

    private static URL getPointsUrl() throws Throwable {
        return new URL((EnjinPlugin.usingSSL ? "https" : "http") + EnjinPlugin.apiurl + "get-points");
    }

    private static String encode(String in) throws UnsupportedEncodingException {
        return URLEncoder.encode(in, "UTF-8");
        //return in;
    }

    public static String parseInput(InputStream in) throws IOException {
        byte[] buffer = new byte[1024];
        int bytesRead = in.read(buffer);
        StringBuilder builder = new StringBuilder();
        while (bytesRead > 0) {
            builder.append(new String(buffer, 0, bytesRead, "UTF-8"));
            bytesRead = in.read(buffer);
        }
        return builder.toString();
    }

}
