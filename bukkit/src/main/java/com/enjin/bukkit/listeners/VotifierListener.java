package com.enjin.bukkit.listeners;

import java.util.ArrayList;

import com.enjin.bukkit.EnjinMinecraftPlugin;
import com.enjin.bukkit.modules.impl.VotifierModule;
import com.enjin.core.Enjin;
import org.bukkit.Bukkit;
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

        String userid = username + "|" + Bukkit.getOfflinePlayer(username).getUniqueId().toString();
        String listname = event.getVote().getServiceName().replaceAll("[^0-9A-Za-z.\\-]", "");

        VotifierModule module = plugin.getModuleManager().getModule(VotifierModule.class);
        if (!module.getPlayerVotes().containsKey(listname)) {
            module.getPlayerVotes().put(listname, new ArrayList<Object[]>());
        }

        module.getPlayerVotes().get(listname).add(new Object[]{userid, System.currentTimeMillis() / 1000});
    }
}
