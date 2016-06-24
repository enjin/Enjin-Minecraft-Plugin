package com.enjin.bukkit.stats;

import java.io.*;
import java.util.Map;

import com.enjin.core.Enjin;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import com.enjin.bukkit.EnjinMinecraftPlugin;

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
				Enjin.getLogger().catching(e);
            }
        }

        try {
            BufferedWriter outChannel = new BufferedWriter(new FileWriter(file));
            String jsonString = getStatsJSON();
            outChannel.write(jsonString);
            outChannel.close();
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
			Enjin.getLogger().catching(e);;
        } catch (IOException e) {
            // TODO Auto-generated catch block
			Enjin.getLogger().catching(e);
        }

        return true;
    }

    @SuppressWarnings("unchecked")
    public String getStatsJSON() {
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
