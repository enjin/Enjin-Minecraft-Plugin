package com.enjin.sponge.stats;

import com.enjin.core.Enjin;
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
                Enjin.getLogger().log(e);
            }
        }

        try {
            BufferedWriter outChannel = new BufferedWriter(new FileWriter(file));
            String jsonString = getStatsJSON();
            outChannel.write(jsonString);
            outChannel.close();
        } catch (Exception e) {
            Enjin.getLogger().log(e);
        }

        return true;
    }

    @SuppressWarnings("unchecked")
    public String getStatsJSON() {
        EnjinMinecraftPlugin plugin = EnjinMinecraftPlugin.getInstance();

        Enjin.getLogger().debug("Getting serialized server stats");
        JSONObject stats = plugin.getServerStats().getSerialized();
        Enjin.getLogger().debug("Creating players array");
        JSONArray players = new JSONArray();

        Enjin.getLogger().debug("Getting all serialized player stats");
        for (Map.Entry<String, StatsPlayer> player : plugin.getPlayerStats().entrySet()) {
            players.add(player.getValue().getSerialized());
        }

        Enjin.getLogger().debug("Adding players to stats");
        stats.put("players", players);
        Enjin.getLogger().debug("Returning as JSON string");
        return JSONValue.toJSONString(stats);
    }

    public String write() {
        return getStatsJSON();
    }
}
