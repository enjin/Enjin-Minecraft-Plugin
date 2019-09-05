package com.enjin.bukkit.tasks;

import com.enjin.bukkit.EnjinMinecraftPlugin;
import org.bukkit.scheduler.BukkitRunnable;

public class ConfigSaver extends BukkitRunnable {

    public static final int PERIOD = 60 * 20;

    @Override
    public void run() {
        EnjinMinecraftPlugin.saveConfiguration();
        EnjinMinecraftPlugin.saveRankUpdatesConfiguration();
    }

    public static void schedule(EnjinMinecraftPlugin plugin) {
        new ConfigSaver().runTaskTimerAsynchronously(plugin, PERIOD, PERIOD);
    }

}
