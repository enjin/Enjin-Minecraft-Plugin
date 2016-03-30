package com.enjin.bukkit.managers;

import com.enjin.bukkit.EnjinMinecraftPlugin;
import com.enjin.bukkit.listeners.VotifierListener;
import com.enjin.core.Enjin;
import org.bukkit.Bukkit;

public class VotifierManager {
    public static void init(EnjinMinecraftPlugin plugin) {
        if (isVotifierEnabled()) {
            Enjin.getPlugin().debug("Initializing votifier support.");
            Bukkit.getPluginManager().registerEvents(new VotifierListener(plugin), plugin);
        }
    }

    public static boolean isVotifierEnabled() {
        return Bukkit.getPluginManager().isPluginEnabled("Votifier");
    }
}
