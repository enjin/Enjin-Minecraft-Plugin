package com.enjin.bukkit.stats;

import com.enjin.bukkit.EnjinMinecraftPlugin;
import com.enjin.core.Enjin;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;

public class WriteStats {
    private EnjinMinecraftPlugin plugin;

    public WriteStats(EnjinMinecraftPlugin plugin) {
        this.plugin = plugin;
    }

    public boolean write(String file) {
        File f = new File(EnjinMinecraftPlugin.getInstance().getDataFolder(), file);
        if (!f.exists()) {
            if (!f.getParentFile().exists()) {
                f.mkdirs();
            }

            try {
                f.createNewFile();
            } catch (IOException e) {
                Enjin.getLogger().log(e);
            }
        }

        try {
            BufferedWriter outChannel = new BufferedWriter(new FileWriter(file));
            String         jsonString = getStatsJSON();
            outChannel.write(jsonString);
            outChannel.close();
        } catch (Exception e) {
            Enjin.getLogger().log(e);
        }

        return true;
    }

    @SuppressWarnings("unchecked")
    public String getStatsJSON() {
        JSONObject stats   = plugin.getServerStats().getSerialized();
        JSONArray  players = new JSONArray();

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
