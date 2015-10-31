package com.enjin.bukkit.managers;

import com.enjin.bukkit.EnjinMinecraftPlugin;
import com.enjin.bukkit.config.EnjinConfig;
import com.enjin.bukkit.listeners.ChatListener;
import com.enjin.bukkit.listeners.EnjinStatsListener;
import com.enjin.bukkit.stats.StatsPlayer;
import com.enjin.bukkit.stats.StatsUtils;
import com.enjin.bukkit.stats.WriteStats;
import com.enjin.bukkit.util.io.FileUtil;
import com.enjin.core.Enjin;
import com.gmail.nossr50.datatypes.skills.SkillType;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerExpChangeEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerMoveEvent;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;

public class StatsManager {
    @Getter
    private static boolean mcMmoEnabled = false;
    private static EnjinStatsListener listener;

    public static void init(EnjinMinecraftPlugin plugin) {
        if (Bukkit.getPluginManager().isPluginEnabled("mcMMO")) {
            try {
                List<SkillType> skills = SkillType.NON_CHILD_SKILLS;
                mcMmoEnabled = true;
            } catch (NoSuchFieldError e) {
                mcMmoEnabled = false;
            } catch (NoClassDefFoundError e) {
                mcMmoEnabled = false;
            } catch (Error e) {
                mcMmoEnabled = false;
            }
        }

        EnjinConfig configuration = EnjinMinecraftPlugin.getConfiguration();
        if (configuration.isCollectPlayerStats()) {
            if (listener == null) {
                Bukkit.getPluginManager().registerEvents(listener = new EnjinStatsListener(plugin), plugin);
                Bukkit.getPluginManager().registerEvents(new ChatListener(), plugin);
            }

            if (!configuration.getStatsCollected().getPlayer().isTravel()) {
                PlayerMoveEvent.getHandlerList().unregister(listener);
            }

            if (!configuration.getStatsCollected().getPlayer().isBlocksBroken()) {
                BlockBreakEvent.getHandlerList().unregister(listener);
            }

            if (!configuration.getStatsCollected().getPlayer().isBlocksPlaced()) {
                BlockPlaceEvent.getHandlerList().unregister(listener);
            }

            if (!configuration.getStatsCollected().getPlayer().isKills()) {
                EntityDeathEvent.getHandlerList().unregister(listener);
            }

            if (!configuration.getStatsCollected().getPlayer().isDeaths()) {
                PlayerDeathEvent.getHandlerList().unregister(listener);
            }

            if (!configuration.getStatsCollected().getPlayer().isXp()) {
                PlayerExpChangeEvent.getHandlerList().unregister(listener);
            }

            if (!configuration.getStatsCollected().getServer().isCreeperExplosions()) {
                EntityExplodeEvent.getHandlerList().unregister(listener);
            }

            if (!configuration.getStatsCollected().getServer().isPlayerKicks()) {
                PlayerKickEvent.getHandlerList().unregister(listener);
            }

            File stats = new File("enjin-stats.json");
            try {
                if (stats.exists()) {
                    String content = FileUtil.readFile(stats, Charset.forName("UTF-8"));
                    StatsUtils.parseStats(content, plugin);
                } else {
                    stats.mkdirs();
                    stats.createNewFile();
                }
            } catch (IOException e) {
                Enjin.getPlugin().debug(e.getMessage());
            }
        }
    }

    public static void disable(EnjinMinecraftPlugin plugin) {
        EnjinConfig configuration = EnjinMinecraftPlugin.getConfiguration();
        if (configuration.isCollectPlayerStats()) {
            new WriteStats(plugin).write("enjin-stats.json");
            Enjin.getPlugin().debug("Stats saved to enjin-stats.json.");
        }
    }

    public static StatsPlayer getPlayerStats(OfflinePlayer player) {
        String uuid = player.getUniqueId().toString().toLowerCase();
        StatsPlayer stats = EnjinMinecraftPlugin.getInstance().getPlayerStats().get(uuid);

        if (stats == null) {
            stats = new StatsPlayer(player);
            EnjinMinecraftPlugin.getInstance().getPlayerStats().put(uuid, stats);
        }

        return stats;
    }

    public static void setPlayerStats(StatsPlayer player) {
        EnjinMinecraftPlugin.getInstance().getPlayerStats().put(player.getUUID().toLowerCase(), player);
    }
}
