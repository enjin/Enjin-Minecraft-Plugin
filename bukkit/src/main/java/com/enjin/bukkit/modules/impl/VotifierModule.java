package com.enjin.bukkit.modules.impl;

import com.enjin.bukkit.EnjinMinecraftPlugin;
import com.enjin.bukkit.config.EMPConfig;
import com.enjin.bukkit.listeners.VotifierListener;
import com.enjin.bukkit.modules.Module;
import com.enjin.core.Enjin;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Module(name = "Votifier", hardPluginDependencies = "Votifier")
public class VotifierModule {
    private EnjinMinecraftPlugin        plugin;
    @Getter
    private Map<String, List<Object[]>> playerVotes = new ConcurrentHashMap<>();
    @Getter
    @Setter
    private int sessionVotes;
    @Setter
    private String lastVote;

    public VotifierModule() {
        this.plugin = EnjinMinecraftPlugin.getInstance();
        init();
    }

    public void init() {
        if (!Enjin.getConfiguration(EMPConfig.class).getEnabledComponents().isVoteListener()) {
            return;
        }

        Enjin.getLogger().debug("Registering vote listener!");
        Bukkit.getPluginManager().registerEvents(new VotifierListener(plugin, this), plugin);
    }

    public Optional<String> getLastVote() {
        return Optional.ofNullable(lastVote);
    }
}
