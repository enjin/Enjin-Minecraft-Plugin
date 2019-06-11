package com.enjin.bukkit.listeners;

import com.enjin.bukkit.EnjinMinecraftPlugin;
import com.enjin.bukkit.modules.impl.VotifierModule;
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
import java.util.regex.Pattern;

public class VotifierListener implements Listener {

    private EnjinMinecraftPlugin plugin;

    public VotifierListener(EnjinMinecraftPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void voteRecieved(VotifierEvent event) {
        Vote vote = event.getVote();
        String username = vote.getUsername().replaceAll("[^0-9A-Za-z_]", "");

        if (username.isEmpty()) return;

        String userid = username;

        if (Bukkit.getOnlineMode()) {
            OfflinePlayer op = Bukkit.getOfflinePlayer(username);
            userid = username + "|" + op.getUniqueId().toString();
        }

        String listName = event.getVote().getServiceName().replaceAll("[^0-9A-Za-z.\\-]", "");
        VotifierModule module = plugin.getModuleManager().getModule(VotifierModule.class);
        if (!module.getPlayerVotes().containsKey(listName)) {
            module.getPlayerVotes().put(listName, new ArrayList<>());
        }
        module.getPlayerVotes().get(listName).add(new Object[] {userid, System.currentTimeMillis() / 1000});
    }
}
