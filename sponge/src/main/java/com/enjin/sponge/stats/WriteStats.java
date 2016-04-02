package com.enjin.sponge.stats;

import com.enjin.sponge.EnjinMinecraftPlugin;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import java.io.*;
import java.util.Map;

public class WriteStats {
    public boolean write(File file) {
		if (file == null) {
			return false;
		}

        if (!file.exists()) {
            if (!file.getParentFile().exists()) {
                file.mkdirs();
            }

            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        try {
            BufferedWriter outChannel = new BufferedWriter(new FileWriter(file));
            String jsonString = getStatsJSON();
            outChannel.write(jsonString);
            outChannel.close();
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return true;
    }

    @SuppressWarnings("unchecked")
    public String getStatsJSON() {
		EnjinMinecraftPlugin plugin = EnjinMinecraftPlugin.getInstance();

        JSONObject stats = plugin.getServerStats().getSerialized();
        JSONArray players = new JSONArray();

        for (Map.Entry<String, StatsPlayer> player : plugin.getPlayerStats().entrySet()) {
            players.add(player.getValue().getSerialized());
        }

        stats.put("players", players);
        return JSONValue.toJSONString(stats);
    }

    public String write() {
        return getStatsJSON();
    }
}
