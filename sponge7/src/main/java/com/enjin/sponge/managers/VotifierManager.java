package com.enjin.sponge.managers;

import com.enjin.core.Enjin;
import com.enjin.sponge.EnjinMinecraftPlugin;
import com.google.common.collect.Lists;
import com.vexsoftware.votifier.model.Vote;
import com.vexsoftware.votifier.sponge.event.VotifierEvent;
import lombok.Getter;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.plugin.PluginContainer;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class VotifierManager {
    @Getter
    private static final Map<String, List<Object[]>> playerVotes      = new ConcurrentHashMap<>();
    @Getter
    private static       boolean                     enabled          = false;
    private static       List<String>                supportedPlugins = Lists.newArrayList();

    static {
        supportedPlugins.add("com.vexsoftware");
        supportedPlugins.add("nuvotifier");
    }

    @Listener
    public void onVote(VotifierEvent event) {
        Vote vote = event.getVote();

        Enjin.getLogger().debug("Received vote from \"" + vote.getUsername() + "\" using \"" + vote.getServiceName());
        if (event.getVote().getUsername().equalsIgnoreCase("test") || event.getVote().getUsername().isEmpty()) {
            return;
        }

        String username = vote.getUsername().replaceAll("[^0-9A-Za-z_]", "");
        if (username.isEmpty() || username.length() > 16) {
            return;
        }

        String userid   = username;
        String listname = event.getVote().getServiceName().replaceAll("[^0-9A-Za-z.\\-]", "");

        Optional<Player> player = Sponge.getServer().getPlayer(userid);
        if (player.isPresent()) {
            userid = username + "|" + player.get().getUniqueId().toString();
        }

        if (!playerVotes.containsKey(listname)) {
            playerVotes.put(listname, new ArrayList<>());
        }

        playerVotes.get(listname).add(new Object[] {userid, System.currentTimeMillis() / 1000});
        Enjin.getLogger()
             .debug("Vote successfully stored for " + vote.getUsername() + " using " + vote.getServiceName());
    }

    public static void init(EnjinMinecraftPlugin plugin) {
        supportedPlugins.forEach(id -> {
            Optional<PluginContainer> optionalContainer = Sponge.getPluginManager().getPlugin(id);
            if (optionalContainer.isPresent()) {
                Enjin.getLogger().info(optionalContainer.get().getName() + " detected, listening for votes.");
                Sponge.getEventManager().registerListeners(plugin, new VotifierManager());
                enabled = true;
                return;
            }
        });
    }
}
