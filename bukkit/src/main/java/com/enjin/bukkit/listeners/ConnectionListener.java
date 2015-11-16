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

        if (!plugin.getNewVersion().equals("") && p.hasPermission("enjin.notify.update")) {
            p.sendMessage(ChatColor.GREEN + "EnjinMinecraftplugin was updated to version " + plugin.getNewVersion() + ". Please restart your server.");
        }

        if (plugin.isUpdateFailed() && p.hasPermission("enjin.notify.failedupdate")) {
            p.sendMessage(ChatColor.DARK_RED + "EnjinMinecraftPlugin failed to update to the newest version. Please download it manually.");
        }

        if (plugin.isAuthKeyInvalid() && p.hasPermission("enjin.notify.invalidauthkey")) {
            p.sendMessage(ChatColor.DARK_RED + "[EnjinMinecraftPlugin] Auth key is invalid. Please generate a new one.");
        }

        if (plugin.isUnableToContactEnjin() && p.hasPermission("enjin.notify.connectionstatus")) {
            p.sendMessage(ChatColor.DARK_RED + "[EnjinMinecraftPlugin] Unable to connect to enjin, please check your settings.");
            p.sendMessage(ChatColor.DARK_RED + "If this problem persists please send enjin the results of the /enjin log");
        }

        if (plugin.isPermissionsNotWorking() && p.hasPermission("enjin.notify.permissionsnotworking")) {
            p.sendMessage(ChatColor.DARK_RED + "[EnjinMinecraftPlugin] Your permissions plugin is not configured correctly. Groups and permissions will not update. Check your server.log for more details.");
        }

        if (VaultManager.isVaultEnabled() && VaultManager.getEconomy() != null && !VaultManager.isEconomyUpToDate() && p.hasPermission("enjin.notify.econoutdated")) {
            p.sendMessage(ChatColor.RED + "[EnjinMinecraftPlugin] " + VaultManager.getEconomy().getName() + " doesn't have UUID support, please update. Using Vault compatibility mode.");
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
        plugin.getPlayerPerms().put(p, uuid);
    }
}
