package com.enjin.officialplugin.threaded;

import java.util.HashMap;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import com.enjin.officialplugin.EnjinMinecraftPlugin;

public class BanLister implements Runnable {

    ConcurrentHashMap<String, String> currentbannedplayers = new ConcurrentHashMap<String, String>();
    EnjinMinecraftPlugin plugin;
    boolean firstrun = true;

    public BanLister(EnjinMinecraftPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void run() {
        if (firstrun) {
            Set<OfflinePlayer> bannedplayerlist = Bukkit.getServer().getBannedPlayers();
            for (OfflinePlayer player : bannedplayerlist) {
                if (player != null && player.getName() != null) {
                    if (EnjinMinecraftPlugin.supportsUUID()) {
                        currentbannedplayers.put(player.getUniqueId().toString(), "");
                    } else {
                        currentbannedplayers.put(player.getName().toLowerCase(), "");
                    }
                }
            }
            firstrun = false;
        } else {
            EnjinMinecraftPlugin.debug("Scanning banned player list");
            Set<OfflinePlayer> bannedplayerlist = Bukkit.getServer().getBannedPlayers();
            HashMap<String, String> lowercasebans = new HashMap<String, String>();
            //Checking for bans being added by console or plugin
            for (OfflinePlayer player : bannedplayerlist) {
                if (player != null && player.getName() != null) {
                    lowercasebans.put(player.getName().toLowerCase(), "");
                    if (!currentbannedplayers.containsKey(player.getName().toLowerCase())) {
                        currentbannedplayers.put(player.getName().toLowerCase(), "");
                        plugin.bannedplayers.put(player.getName().toLowerCase(), "");
                        EnjinMinecraftPlugin.debug("Adding banned player " + player.getName());
                    }
                }
            }
            //checking for pardons being done by console or plugin
            Set<String> keys = currentbannedplayers.keySet();
            for (String player : keys) {
                if (!lowercasebans.containsKey(player)) {
                    currentbannedplayers.remove(player);
                    plugin.pardonedplayers.put(player, "");
                    EnjinMinecraftPlugin.debug(player + " was pardoned. Adding to pardoned list.");
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
        if (EnjinMinecraftPlugin.supportsUUID()) {
            currentbannedplayers.put(name.getUniqueId().toString(), "");
        } else {
            currentbannedplayers.put(name.getName().toLowerCase(), "");
        }
    }


    /**
     * Remove a banned player to the list without adding them to the pardoned players
     * in the plugin
     *
     * @param name Name of the player that got pardoned.
     */
    public synchronized void pardonBannedPlayer(OfflinePlayer name) {
        if (EnjinMinecraftPlugin.supportsUUID()) {
            currentbannedplayers.remove(name.getUniqueId().toString());
        } else {
            currentbannedplayers.remove(name.getName().toLowerCase());
        }
    }

    /**
     * Is the player in the ban list?
     *
     * @param name Name of the player to check
     * @return true if the player is banned, false otherwise.
     */
    public synchronized boolean playerIsBanned(OfflinePlayer name) {
        if (EnjinMinecraftPlugin.supportsUUID()) {
            return currentbannedplayers.containsKey(name.getUniqueId().toString());
        } else {
            return currentbannedplayers.containsKey(name.getName().toLowerCase());
        }
    }

}
