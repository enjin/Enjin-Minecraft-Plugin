package com.enjin.bukkit.stats;

import com.enjin.bukkit.managers.StatsManager;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.enjin.bukkit.EnjinMinecraftPlugin;

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
                    StatsManager.setPlayerStats(splayer);
                }
            }
        } catch (ParseException e) {
            //No more stacktraces please!
            //e.printStackTrace();
        }
    }
}
