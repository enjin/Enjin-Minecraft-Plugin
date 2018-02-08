package com.enjin.bukkit.listeners;

import java.util.ArrayList;

import com.enjin.bukkit.EnjinMinecraftPlugin;
import com.enjin.bukkit.modules.impl.VotifierModule;
import com.enjin.core.Enjin;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import com.vexsoftware.votifier.model.Vote;
import com.vexsoftware.votifier.model.VotifierEvent;

public class VotifierListener implements Listener {
    EnjinMinecraftPlugin plugin;

    public VotifierListener(EnjinMinecraftPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void voteRecieved(VotifierEvent event) {
        Vote vote = event.getVote();

        Enjin.getLogger().debug("Received vote from \"" + vote.getUsername() + "\" using \"" + vote.getServiceName());
        if (event.getVote().getUsername().equalsIgnoreCase("test") || event.getVote().getUsername().isEmpty()) {
            return;
        }

        String username = vote.getUsername().replaceAll("[^0-9A-Za-z_]", "");
        if (username.isEmpty() || username.length() > 16) {
            return;
        }

        OfflinePlayer player = Bukkit.getPlayer(username);
        if (player == null) {
            for (OfflinePlayer op : Bukkit.getOfflinePlayers()) {
                if (op.getName() != null & op.getName().equalsIgnoreCase(username))
                    player = op;

                if (player != null)
                    break;
            }
        }

        if (player != null) {
            String userId = username + "|" + player.getUniqueId().toString();
            String listName = event.getVote().getServiceName().replaceAll("[^0-9A-Za-z.\\-]", "");

            VotifierModule module = plugin.getModuleManager().getModule(VotifierModule.class);
            if (!module.getPlayerVotes().containsKey(listName))
                module.getPlayerVotes().put(listName, new ArrayList<Object[]>());

            module.getPlayerVotes().get(listName).add(new Object[]{userId, System.currentTimeMillis() / 1000});
        } else {
            Enjin.getLogger().debug("Could not find correspond player of vote: " + username);
        }
    }
}
