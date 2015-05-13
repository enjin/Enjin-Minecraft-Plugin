package com.enjin.officialplugin.stats;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.enjin.officialplugin.EnjinMinecraftPlugin;

public class StatsUtils {

    public static void parseStats(String stats, EnjinMinecraftPlugin plugin) {

        JSONParser parser = new JSONParser();
        try {
            JSONObject jstats = (JSONObject) parser.parse(stats);
            JSONArray array = (JSONArray) jstats.get("players");
            for (Object oitem : array) {
                if (oitem instanceof JSONObject) {
                    JSONObject item = (JSONObject) oitem;
                    StatsPlayer splayer = new StatsPlayer(item);
                    plugin.setPlayerStats(splayer);
                }
            }
        } catch (ParseException e) {
            //No more stacktraces please!
            //e.printStackTrace();
        }
    }

    public static int getInt(Object object) {
        int length = 0;
        if (object == null) {
            return length;
        }
        if (object instanceof Double || object instanceof Float) {
            if (object instanceof Double) {
                length = ((Double) object).intValue();
            } else {
                length = ((Float) object).intValue();
            }
        } else if (object instanceof Integer) {
            length = ((Integer) object);
        } else {
            try {
                length = Integer.parseInt(object.toString());
            } catch (NumberFormatException e) {

            }
        }
        return length;
    }

    public static double getDouble(Object object) {
        double length = 0;
        if (object == null) {
            return length;
        }
        if (object instanceof String) {
            try {
                length = Double.parseDouble((String) object);
            } catch (NumberFormatException e) {

            }
        } else if (object instanceof Double || object instanceof Float) {
            if (object instanceof Double) {
                length = ((Double) object);
            } else {
                length = ((Float) object);
            }
        } else if (object instanceof Integer) {
            length = ((Integer) object);
        }
        return length;
    }

}
