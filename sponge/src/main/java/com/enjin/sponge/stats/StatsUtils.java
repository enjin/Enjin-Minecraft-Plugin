package com.enjin.sponge.stats;

import com.enjin.sponge.managers.StatsManager;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class StatsUtils {
    public static void parseStats(String stats) {
        JSONParser parser = new JSONParser();
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
