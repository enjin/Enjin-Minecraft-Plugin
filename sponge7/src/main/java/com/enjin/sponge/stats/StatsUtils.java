package com.enjin.sponge.stats;

import com.enjin.sponge.managers.StatsManager;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import net.minidev.json.parser.JSONParser;
import net.minidev.json.parser.ParseException;

public class StatsUtils {
    public static void parseStats(String stats) {
        JSONParser parser = new JSONParser(JSONParser.MODE_JSON_SIMPLE);
        try {
            JSONObject jstats = (JSONObject) parser.parse(stats);
            JSONArray array = (JSONArray) jstats.get("players");
            for (Object oitem : array) {
                if (oitem instanceof JSONObject) {
                    JSONObject item = (JSONObject) oitem;
                    StatsPlayer splayer = new StatsPlayer(item);
                    StatsManager.setPlayerStats(splayer);
                }
            }
        } catch (ParseException e) {
            //No more stacktraces please!
            //Enjin.getLogger().log(e);
        }
    }
}
