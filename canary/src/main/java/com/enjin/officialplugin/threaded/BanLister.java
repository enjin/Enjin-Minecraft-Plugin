package com.enjin.officialplugin.threaded;

import java.util.HashMap;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import net.canarymod.Canary;
import net.canarymod.bansystem.Ban;

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
            Ban[] bannedplayerlist = Canary.bans().getAllBans();
            for (Ban ban : bannedplayerlist) {
                if (!ban.isIpBan()) {
                    currentbannedplayers.put(ban.getSubject().toLowerCase(), "");
                }
            }
            firstrun = false;
        } else {
            EnjinMinecraftPlugin.debug("Scanning banned player list");
            Ban[] bannedplayerlist = Canary.bans().getAllBans();
            HashMap<String, String> lowercasebans = new HashMap<String, String>();
            //Checking for bans being added by console or plugin
            for (Ban ban : bannedplayerlist) {
                if (!ban.isIpBan()) {
                    lowercasebans.put(ban.getSubject().toLowerCase(), "");
                    if (!currentbannedplayers.containsKey(ban.getSubject().toLowerCase())) {
                        currentbannedplayers.put(ban.getSubject().toLowerCase(), "");
                        plugin.bannedplayers.put(ban.getSubject().toLowerCase(), "");
                        EnjinMinecraftPlugin.debug("Adding banned player " + ban.getSubject());
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
