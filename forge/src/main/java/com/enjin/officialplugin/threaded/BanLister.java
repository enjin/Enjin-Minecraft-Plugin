package com.enjin.officialplugin.threaded;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.management.BanEntry;
import net.minecraft.server.management.BanList;

import com.enjin.officialplugin.EnjinMinecraftPlugin;

public class BanLister implements Runnable {

    ConcurrentHashMap<String, String> currentbannedplayers = new ConcurrentHashMap<String, String>();
    EnjinMinecraftPlugin plugin;

    public BanLister(EnjinMinecraftPlugin plugin) {
        this.plugin = plugin;
        Set<String> bannedplayerlist = MinecraftServer.getServer().getConfigurationManager().getBannedPlayers().getBannedList().keySet();
        for (String player : bannedplayerlist) {
            currentbannedplayers.put(player.toLowerCase(), "");
        }
    }

    @Override
    public void run() {
        plugin.debug("Scanning banned player list");
        Set<String> bannedplayerlist = MinecraftServer.getServer().getConfigurationManager().getBannedPlayers().getBannedList().keySet();
        HashMap<String, String> lowercasebans = new HashMap<String, String>();
        //Checking for bans being added by console or plugin
        for (String player : bannedplayerlist) {
            lowercasebans.put(player.toLowerCase(), "");
            if (!currentbannedplayers.containsKey(player.toLowerCase())) {
                currentbannedplayers.put(player.toLowerCase(), "");
                plugin.bannedplayers.put(player.toLowerCase(), "");
                plugin.debug("Adding banned player " + player);
            }
        }
        //checking for pardons being done by console or plugin
        Set<String> keys = currentbannedplayers.keySet();
        for (String player : keys) {
            if (!lowercasebans.containsKey(player)) {
                currentbannedplayers.remove(player);
                plugin.pardonedplayers.put(player, "");
                plugin.debug(player + " was pardoned. Adding to pardoned list.");
            }
        }
    }

    /**
     * Add a banned player to the list without adding them to the banned players
     * in the plugin
     *
     * @param name Name of the player that got banned.
     */
    public synchronized void addBannedPlayer(String name) {
        currentbannedplayers.put(name.toLowerCase(), "");
    }


    /**
     * Remove a banned player to the list without adding them to the pardoned players
     * in the plugin
     *
     * @param name Name of the player that got pardoned.
     */
    public synchronized void pardonBannedPlayer(String name) {
        currentbannedplayers.remove(name.toLowerCase());
    }

    /**
     * Is the player in the ban list?
     *
     * @param name Name of the player to check
     * @return true if the player is banned, false otherwise.
     */
    public synchronized boolean playerIsBanned(String name) {
        return currentbannedplayers.containsKey(name.toLowerCase());
    }

}
