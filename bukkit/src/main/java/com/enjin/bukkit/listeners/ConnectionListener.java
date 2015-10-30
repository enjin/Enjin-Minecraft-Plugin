package com.enjin.bukkit.listeners;

import com.enjin.bukkit.EnjinMinecraftPlugin;
import com.enjin.bukkit.managers.VaultManager;
import lombok.Getter;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class ConnectionListener implements Listener {
    @Getter
    private static ConnectionListener instance;
    private EnjinMinecraftPlugin plugin;

    public ConnectionListener(EnjinMinecraftPlugin plugin) {
        ConnectionListener.instance = this;
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent e) {
        Player p = e.getPlayer();
        updatePlayerRanks(p);

        if (!plugin.newversion.equals("") && p.hasPermission("enjin.notify.update")) {
            p.sendMessage(ChatColor.GREEN + "EnjinMinecraftplugin was updated to version " + plugin.newversion + ". Please restart your server.");
        }

        if (plugin.updatefailed && p.hasPermission("enjin.notify.failedupdate")) {
            p.sendMessage(ChatColor.DARK_RED + "EnjinMinecraftPlugin failed to update to the newest version. Please download it manually.");
        }

        if (plugin.authkeyinvalid && p.hasPermission("enjin.notify.invalidauthkey")) {
            p.sendMessage(ChatColor.DARK_RED + "[EnjinMinecraftPlugin] Auth key is invalid. Please generate a new one.");
        }

        if (plugin.votifiererrored && p.hasPermission("enjin.notify.votifiererrored")) {
            p.sendMessage(ChatColor.DARK_RED + "[EnjinMinecraftPlugin] Votifier is not configured correctly. Voting rewards will not work.");
            p.sendMessage(ChatColor.RED + "For more information visit: http://www.enjin.com/info/votifier");
        }

        if (plugin.unabletocontactenjin && p.hasPermission("enjin.notify.connectionstatus")) {
            p.sendMessage(ChatColor.DARK_RED + "[EnjinMinecraftPlugin] Unable to connect to enjin, please check your settings.");
            p.sendMessage(ChatColor.DARK_RED + "If this problem persists please send enjin the results of the /enjin log");
        }

        if (plugin.permissionsnotworking && p.hasPermission("enjin.notify.permissionsnotworking")) {
            p.sendMessage(ChatColor.DARK_RED + "[EnjinMinecraftPlugin] Your permissions plugin is not configured correctly. Groups and permissions will not update. Check your server.log for more details.");
        }

        if (plugin.vaultneedsupdating && p.hasPermission("enjin.notify.permissionsnotworking")) {
            p.sendMessage(ChatColor.DARK_RED + "[EnjinMinecraftPlugin] Your version of Vault is outdated. Groups and permissions will not update.");
            p.sendMessage(ChatColor.DARK_RED + "Download the latest version here: " + ChatColor.GOLD + "http://dev.bukkit.org/bukkit-plugins/vault/files/");
        }

        if (plugin.gmneedsupdating && p.hasPermission("enjin.notify.permissionsnotworking")) {
            p.sendMessage(ChatColor.DARK_RED + "[EnjinMinecraftPlugin] Your version of GroupManager is outdated. Groups and permissions will not update correctly.");
            p.sendMessage(ChatColor.DARK_RED + "Download the latest version here: " + ChatColor.GOLD + "http://tiny.cc/EssentialsGMZip");
        }

        if (VaultManager.isVaultEnabled() && VaultManager.getEconomy() != null && !VaultManager.isEconomyUpToDate() && p.hasPermission("enjin.notify.econoutdated")) {
            p.sendMessage(ChatColor.RED + "[EnjinMinecraftPlugin] " + VaultManager.getEconomy().getName() + " doesn't have UUID support, please update. Using Vault compatibility mode.");
        }

        if (EnjinMinecraftPlugin.isMcmmoOutdated() && p.hasPermission("enjin.notify.mcmmooutdated")) {
            p.sendMessage(ChatColor.RED + "[EnjinMinecraftPlugin] Your version of mcMMO is out of date! Please update to collect mcMMO stats:");
            p.sendMessage(ChatColor.RED + "http://dev.bukkit.org/bukkit-plugins/mcmmo/");
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerQuit(PlayerQuitEvent e) {
        Player p = e.getPlayer();
        updatePlayerRanks(p);
    }

    public void updatePlayerRanks(Player p) {
        updatePlayerRanks(p.getName(), p.getUniqueId().toString());
    }

    public void updatePlayerRanks(String p, String uuid) {
        plugin.playerperms.put(p, uuid);
    }
}
