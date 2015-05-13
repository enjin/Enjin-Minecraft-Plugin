package com.enjin.officialplugin.listeners;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import com.enjin.officialplugin.EnjinMinecraftPlugin;
import com.enjin.officialplugin.heads.HeadData;
import com.enjin.officialplugin.heads.HeadLocation;
import com.vexsoftware.votifier.model.Vote;
import com.vexsoftware.votifier.model.VotifierEvent;

public class VotifierListener implements Listener {

    EnjinMinecraftPlugin plugin;
    SimpleDateFormat date = new SimpleDateFormat("dd MMM yyyy");
    SimpleDateFormat time = new SimpleDateFormat("h:mm:ss a z");

    public VotifierListener(EnjinMinecraftPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void voteRecieved(VotifierEvent event) {
        //Lists when testing will send a user called "test", let's
        //make sure we don't process those votes.
        if (event.getVote().getUsername().equalsIgnoreCase("test") ||
                event.getVote().getUsername().isEmpty()) {
            return;
        }
        Vote vote = event.getVote();
        //Remove anything non-alphanumeric from the username, removing exploits
        String username = vote.getUsername().replaceAll("[^0-9A-Za-z_]", "");
        if (username.isEmpty()) return;
        String userid = username;
        if (EnjinMinecraftPlugin.supportsUUID()) {
            OfflinePlayer op = Bukkit.getOfflinePlayer(username);
            userid = username + "|" + op.getUniqueId().toString();
        }
        String lists = "";
        if (plugin.playervotes.containsKey(userid)) {
            lists = plugin.playervotes.get(userid);
            lists = lists + "," + vote.getServiceName().replaceAll("[^0-9A-Za-z.\\-]", "");
        } else {
            lists = vote.getServiceName().replaceAll("[^0-9A-Za-z.\\-]", "");
        }
        //We need to convert from Unix time stamp to a date stamp we can work with.
        long realvotetime = System.currentTimeMillis();
        Date votedate = new Date(realvotetime);
        String voteday = date.format(votedate);
        String svotetime = time.format(votedate);

        try {
        } catch (NumberFormatException e) {

        }

        String[] signdata = plugin.cachedItems.getSignData(username, voteday, HeadLocation.Type.RecentVoter, 0, svotetime);
        HeadData hd = new HeadData(username, signdata, HeadLocation.Type.RecentVoter, 0);
        plugin.headdata.addToHead(hd, true);
        plugin.playervotes.put(userid, lists);
    }

}
