package com.enjin.bukkit.listeners;

import com.enjin.bukkit.EnjinMinecraftPlugin;
import com.enjin.bukkit.listeners.perm.processors.PermissionsBukkitListener;
import com.enjin.bukkit.managers.VaultManager;
import com.enjin.core.Enjin;
import com.enjin.rpc.mappings.mappings.plugin.PlayerGroupInfo;
import lombok.Getter;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.*;

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

    public static void updatePlayerRanks(OfflinePlayer player) {
        updatePlayerRanks1(player);
        EnjinMinecraftPlugin.saveRankUpdatesConfiguration();
    }

    public static void updatePlayerRanks1(OfflinePlayer player) {
        if (player == null || player.getName() == null) {
            Enjin.getLogger().debug("[ConnectionListener::updatePlayerRanks] Player or their name is null. Unable to update their ranks.");
            return;
        }

        PlayerGroupInfo info = new PlayerGroupInfo(player.getUniqueId());

        if (info == null) {
            Enjin.getLogger().debug("[ConnectionListener::updatePlayerRanks] PlayerGroupInfo is null. Unable to update " + player.getName() + "'s ranks.");
            return;
        }

        if (EnjinMinecraftPlugin.getRankUpdatesConfiguration() == null) {
            Enjin.getLogger().debug("[ConnectionListener::updatePlayerRanks] RankUpdatesConfiguration is null.");
            return;
        }

        if (EnjinMinecraftPlugin.getRankUpdatesConfiguration().getPlayerPerms() == null) {
            Enjin.getLogger().debug("[ConnectionListener::updatePlayerRanks] Player perms is null.");
            return;
        }

        info.getWorlds().putAll(getPlayerGroups(player));
        EnjinMinecraftPlugin.getRankUpdatesConfiguration().getPlayerPerms().put(player.getName(), info);
    }

    public static void updatePlayersRanks(OfflinePlayer[] players) {
        for (OfflinePlayer player : players) {
            updatePlayerRanks1(player);
        }

        EnjinMinecraftPlugin.saveRankUpdatesConfiguration();
    }

    public static Map<String, List<String>> getPlayerGroups(OfflinePlayer player) {
        Map<String, List<String>> groups = new HashMap<>();

        if (VaultManager.isVaultEnabled() && VaultManager.isPermissionsAvailable()) {
            Permission permission = VaultManager.getPermission();
            if (permission.hasGroupSupport()) {
                String[] g = permission.getPlayerGroups(null, player);
				if (g != null) {
					if (g.length == 0 && Bukkit.getPluginManager().isPluginEnabled("PermissionsBukkit")) {
						g = PermissionsBukkitListener.getGroups(player);
					}

					if (g.length > 0) {
						groups.put("*", Arrays.asList(g));
					}

					for (World world : Bukkit.getWorlds()) {
						g = permission.getPlayerGroups(world.getName(), player);
						if (g.length > 0) {
							groups.put(world.getName(), Arrays.asList(g));
						}
					}
				}
            }
        }

        return groups;
    }
}
