package com.enjin.bukkit.tasks;

import com.enjin.bukkit.EnjinMinecraftPlugin;
import com.enjin.core.Enjin;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

public class BanLister implements Runnable {
    @Getter
    private static BanLister            instance;
    private        EnjinMinecraftPlugin plugin;
    private        List<String>         currentBannedPlayers = new CopyOnWriteArrayList<>();
    private        boolean              firstRun             = true;

    public BanLister(EnjinMinecraftPlugin plugin) {
        BanLister.instance = this;
        this.plugin = plugin;
    }

    @Override
    public void run() {
        Set<OfflinePlayer> bannedPlayersList = Bukkit.getServer().getBannedPlayers();

        if (firstRun) {
            for (OfflinePlayer player : bannedPlayersList) {
                if (player != null && player.getName() != null) {
                    currentBannedPlayers.add(player.getName());
                }
            }

            firstRun = false;
        } else {
            Enjin.getLogger().debug("Scanning banned player list");
            List<String> lowercaseBans = new ArrayList<>();

            //Checking for bans being added by console or plugin
            for (OfflinePlayer player : bannedPlayersList) {
                if (player != null && player.getName() != null) {
                    lowercaseBans.add(player.getName().toLowerCase());
                    if (!currentBannedPlayers.contains(player.getName().toLowerCase())) {
                        currentBannedPlayers.add(player.getName().toLowerCase());
                        plugin.getBannedPlayers().put(player.getName().toLowerCase(), "");
                        Enjin.getLogger().debug("Adding banned player " + player.getName());
                    }
                }
            }

            for (String player : new ArrayList<>(currentBannedPlayers)) {
                if (!lowercaseBans.contains(player.toLowerCase())) {
                    currentBannedPlayers.remove(player.toLowerCase());
                    plugin.getPardonedPlayers().put(player.toLowerCase(), "");
                    Enjin.getLogger().debug(player + " was pardoned. Adding to pardoned list.");
                }
            }
        }
    }

    /**
     * Add a banned player to the list without adding them to the banned players
     * in the plugin
     */
    public synchronized void addBannedPlayer(OfflinePlayer op) {
        currentBannedPlayers.add(op.getName().toLowerCase());
    }


    /**
     * Remove a banned player to the list without adding them to the pardoned players
     * in the plugin
     */
    public synchronized void pardonBannedPlayer(OfflinePlayer op) {
        currentBannedPlayers.remove(op.getName().toLowerCase());
    }

    /**
     * Is the player in the ban list?
     *
     * @return true if the player is banned, false otherwise.
     */
    public synchronized boolean playerIsBanned(OfflinePlayer op) {
        return currentBannedPlayers.contains(op.getName().toLowerCase());
    }

}
