package com.enjin.bukkit.listeners;

import com.enjin.bukkit.EnjinMinecraftPlugin;
import com.enjin.bukkit.modules.impl.VotifierModule;
import com.enjin.bukkit.util.UUIDFetcher;
import com.enjin.core.Enjin;
import com.enjin.core.util.Timer;
import com.vexsoftware.votifier.model.Vote;
import com.vexsoftware.votifier.model.VotifierEvent;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import java.util.ArrayList;
import java.util.UUID;
import java.util.regex.Pattern;

public class VotifierListener implements Listener {

    private EnjinMinecraftPlugin plugin;
    private VotifierModule module;

    public VotifierListener(EnjinMinecraftPlugin plugin, VotifierModule module) {
        this.plugin = plugin;
        this.module = module;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void voteRecieved(VotifierEvent event) {
        final Vote vote = event.getVote();
        final String username = vote.getUsername().replaceAll("[^0-9A-Za-z_]", "");
        final String listName = event.getVote().getServiceName().replaceAll("[^0-9A-Za-z.\\-]", "");

        if (username.isEmpty() || listName.isEmpty()) return;

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            String userid = username;

            if (Bukkit.getOnlineMode()) {
                try {
                    UUID uuid = UUIDFetcher.getUUIDOf(userid);
                    if (uuid != null) userid = username + "|" + uuid.toString();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            if (!module.getPlayerVotes().containsKey(listName)) {
                module.getPlayerVotes().put(listName, new ArrayList<>());
            }

            module.getPlayerVotes().get(listName).add(new Object[]{userid, System.currentTimeMillis() / 1000});
            module.setSessionVotes(module.getSessionVotes() + 1);
            module.setLastVote(String.format("%s voted on %s at %s", username, listName, vote.getTimeStamp()));
        });
    }
}
