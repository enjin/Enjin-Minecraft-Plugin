package com.enjin.bukkit.managers;

import com.enjin.bukkit.EnjinMinecraftPlugin;
import com.enjin.bukkit.listeners.VotifierListener;
import com.enjin.bukkit.tasks.VoteSender;
import com.enjin.core.Enjin;
import org.bukkit.Bukkit;

public class VotifierManager {
    public static void init(EnjinMinecraftPlugin plugin) {
        if (isVotifierEnabled()) {
            Enjin.getPlugin().debug("Initializing votifier support.");
            Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, new VoteSender(plugin), 20L * 60L, 20L * 60L).getTaskId();
            Bukkit.getPluginManager().registerEvents(new VotifierListener(plugin), plugin);
        }
    }

    public static boolean isVotifierEnabled() {
        return Bukkit.getPluginManager().isPluginEnabled("Votifier");
    }
}
