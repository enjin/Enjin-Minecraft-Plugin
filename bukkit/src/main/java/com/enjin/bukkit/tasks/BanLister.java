package com.enjin.bukkit.tasks;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.enjin.core.Enjin;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import com.enjin.bukkit.EnjinMinecraftPlugin;

public class BanLister implements Runnable {
    @Getter
    private static BanLister instance;
    private EnjinMinecraftPlugin plugin;
    private Map<String, String> currentbannedplayers = new ConcurrentHashMap<String, String>();
    private boolean firstrun = true;

    public BanLister(EnjinMinecraftPlugin plugin) {
        BanLister.instance = this;
        this.plugin = plugin;
    }

    @Override
    public void run() {
        if (firstrun) {
            Set<OfflinePlayer> bannedplayerlist = Bukkit.getServer().getBannedPlayers();

            for (OfflinePlayer player : bannedplayerlist) {
                if (player != null && player.getName() != null) {
                    currentbannedplayers.put(player.getUniqueId().toString(), "");
                }
            }

            firstrun = false;
        } else {
            Enjin.getPlugin().debug("Scanning banned player list");
            Set<OfflinePlayer> bannedplayerlist = Bukkit.getServer().getBannedPlayers();
            HashMap<String, String> lowercasebans = new HashMap<String, String>();

            //Checking for bans being added by console or plugin
            for (OfflinePlayer player : bannedplayerlist) {
                if (player != null && player.getName() != null) {
                    lowercasebans.put(player.getName().toLowerCase(), "");
                    if (!currentbannedplayers.containsKey(player.getName().toLowerCase())) {
                        currentbannedplayers.put(player.getName().toLowerCase(), "");
                        plugin.getBannedPlayers().put(player.getName().toLowerCase(), "");
                        Enjin.getPlugin().debug("Adding banned player " + player.getName());
                    }
                }
            }

            //checking for pardons being done by console or plugin
            Set<String> keys = currentbannedplayers.keySet();
            for (String player : keys) {
                if (!lowercasebans.containsKey(player)) {
                    currentbannedplayers.remove(player);
                    plugin.getPardonedPlayers().put(player, "");
                    Enjin.getPlugin().debug(player + " was pardoned. Adding to pardoned list.");
                }
            }
        }
    }

    /**
     * Add a banned player to the list without adding them to the banned players
     * in the plugin
     *
     * @param name Name of the player that got banned.
     */
    public synchronized void addBannedPlayer(OfflinePlayer name) {
        currentbannedplayers.put(name.getUniqueId().toString(), "");
    }


    /**
     * Remove a banned player to the list without adding them to the pardoned players
     * in the plugin
     *
     * @param name Name of the player that got pardoned.
     */
    public synchronized void pardonBannedPlayer(OfflinePlayer name) {
        currentbannedplayers.remove(name.getUniqueId().toString());
    }

    /**
     * Is the player in the ban list?
     *
     * @param name Name of the player to check
     * @return true if the player is banned, false otherwise.
     */
    public synchronized boolean playerIsBanned(OfflinePlayer name) {
        return currentbannedplayers.containsKey(name.getUniqueId().toString());
    }

}
