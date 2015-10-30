package com.enjin.bukkit.managers;

import com.enjin.bukkit.EnjinMinecraftPlugin;
import com.enjin.bukkit.stats.StatsPlayer;
import org.bukkit.OfflinePlayer;

public class StatsManager {
    public static StatsPlayer getPlayerStats(OfflinePlayer player) {
        String uuid = player.getUniqueId().toString().toLowerCase();
        StatsPlayer stats = EnjinMinecraftPlugin.getInstance().playerstats.get(uuid);

        if (stats == null) {
            stats = new StatsPlayer(player);
            EnjinMinecraftPlugin.getInstance().playerstats.put(uuid, stats);
        }

        return stats;
    }

    public static void setPlayerStats(StatsPlayer player) {
        EnjinMinecraftPlugin.getInstance().playerstats.put(player.getUUID().toLowerCase(), player);
    }
}
