package com.enjin.bukkit.stats;

import com.enjin.bukkit.modules.impl.StatsModule;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.enjin.bukkit.EnjinMinecraftPlugin;

public class StatsUtils {
    public static void parseStats(String stats, EnjinMinecraftPlugin plugin) {
		StatsModule module = EnjinMinecraftPlugin.getInstance().getModuleManager().getModule(StatsModule.class);
		if (module != null) {
			JSONParser parser = new JSONParser();
			try {
				JSONObject jstats = (JSONObject) parser.parse(stats);
				JSONArray array = (JSONArray) jstats.get("players");
				for (Object oitem : array) {
					if (oitem instanceof JSONObject) {
						JSONObject item = (JSONObject) oitem;
						StatsPlayer splayer = new StatsPlayer(item);
						module.setPlayerStats(splayer);
					}
				}
			} catch (ParseException e) {}
		}
    }
}
