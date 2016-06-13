package com.enjin.sponge.managers;

import com.enjin.core.Enjin;
import com.enjin.sponge.EnjinMinecraftPlugin;
import com.enjin.sponge.config.EMPConfig;
import com.enjin.sponge.listeners.ChatListener;
import com.enjin.sponge.listeners.EnjinStatsListener;
import com.enjin.sponge.stats.StatsPlayer;
import com.enjin.sponge.stats.StatsUtils;
import com.enjin.sponge.stats.WriteStats;
import com.enjin.sponge.utils.io.FileUtil;
import lombok.Getter;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;

public class StatsManager {
    private static EnjinStatsListener listener;
	@Getter
	private static File statFile;

    public static void init(EnjinMinecraftPlugin plugin) {
        int i = 1;
        statFile = new File(plugin.getConfigDir(), "enjin-stats.json");
        try {
            if (statFile.exists()) {
                String content = FileUtil.readFile(statFile, Charset.forName("UTF-8"));
                if (content != null && !content.isEmpty()) {
                    StatsUtils.parseStats(content);
                }
            } else {
				statFile.createNewFile();
            }
        } catch (IOException e) {
            Enjin.getPlugin().debug(e.getMessage());
        }

        EMPConfig configuration = Enjin.getConfiguration(EMPConfig.class);
        if (configuration.isCollectPlayerStats()) {
            if (listener == null) {
				Sponge.getEventManager().registerListeners(Enjin.getPlugin(), listener = new EnjinStatsListener(plugin));
				Sponge.getEventManager().registerListeners(Enjin.getPlugin(), new ChatListener());
            }
        }
    }

    public static void disable() {
        EMPConfig configuration = Enjin.getConfiguration(EMPConfig.class);
        if (configuration.isCollectPlayerStats()) {
            new WriteStats().write(statFile);
            Enjin.getPlugin().debug("Stats saved to enjin-stats.json.");
        }
    }

    public static StatsPlayer getPlayerStats(Player player) {
        if (player.getUniqueId() == null) {
            return null;
        }

        String uuid = player.getUniqueId().toString().toLowerCase();
        StatsPlayer stats = EnjinMinecraftPlugin.getInstance().getPlayerStats().get(uuid);

        if (stats == null) {
            stats = new StatsPlayer(player);
            EnjinMinecraftPlugin.getInstance().getPlayerStats().put(uuid, stats);
        }

        return stats;
    }

    public static void setPlayerStats(StatsPlayer player) {
        EnjinMinecraftPlugin.getInstance().getPlayerStats().put(player.getUuid().toLowerCase(), player);
    }
}
