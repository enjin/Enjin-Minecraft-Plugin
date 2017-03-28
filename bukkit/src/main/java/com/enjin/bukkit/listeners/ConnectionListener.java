package com.enjin.bukkit.listeners;

import com.enjin.bukkit.EnjinMinecraftPlugin;
import com.enjin.bukkit.modules.impl.VaultModule;
import com.enjin.bukkit.util.PermissionsUtil;
import com.enjin.bukkit.util.Plugins;
import com.enjin.core.Enjin;
import com.enjin.rpc.mappings.mappings.plugin.PlayerGroupInfo;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.Plugin;

public class ConnectionListener implements Listener {
    @Getter
    private static ConnectionListener instance;
    private EnjinMinecraftPlugin plugin;

    public ConnectionListener(EnjinMinecraftPlugin plugin) {
        ConnectionListener.instance = this;
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerLogin(PlayerLoginEvent e) {
        final Player p = e.getPlayer();
        updatePlayerRanks(p);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent e) {
        Player p = e.getPlayer();

        if (!plugin.getNewVersion().equals("") && PermissionsUtil.hasPermission(p, "enjin.notify.update")) {
            p.sendMessage(ChatColor.GREEN + "EnjinMinecraftplugin was updated to version " + plugin.getNewVersion() + ". Please restart your server.");
        }

        if (plugin.isUpdateFailed() && PermissionsUtil.hasPermission(p, "enjin.notify.failedupdate")) {
            p.sendMessage(ChatColor.DARK_RED + "EnjinMinecraftPlugin failed to update to the newest version. Please download it manually.");
        }

        if (plugin.isAuthKeyInvalid() && PermissionsUtil.hasPermission(p, "enjin.notify.invalidauthkey")) {
            p.sendMessage(ChatColor.DARK_RED + "[EnjinMinecraftPlugin] Auth key is invalid. Please generate a new one.");
        }

        if (plugin.isUnableToContactEnjin() && PermissionsUtil.hasPermission(p, "enjin.notify.connectionstatus")) {
            p.sendMessage(ChatColor.DARK_RED + "[EnjinMinecraftPlugin] Unable to connect to enjin, please check your settings.");
            p.sendMessage(ChatColor.DARK_RED + "If this problem persists please send enjin the results of the /enjin log");
        }

        if (plugin.isPermissionsNotWorking() && PermissionsUtil.hasPermission(p, "enjin.notify.permissionsnotworking")) {
            p.sendMessage(ChatColor.DARK_RED + "[EnjinMinecraftPlugin] Your permissions plugin is not configured correctly. Groups and permissions will not update. Check your server.log for more details.");
        }

        VaultModule module = plugin.getModuleManager().getModule(VaultModule.class);
        if (module != null && module.isEconomyAvailable() && !module.isEconomyUpToDate() && PermissionsUtil.hasPermission(p, "enjin.notify.econoutdated")) {
            p.sendMessage(ChatColor.RED + "[EnjinMinecraftPlugin] " + module.getEconomy().getName() + " doesn't have UUID support, please update. Using Vault compatibility mode.");
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
        VaultModule module = EnjinMinecraftPlugin.getInstance().getModuleManager().getModule(VaultModule.class);
        if (module == null || !module.isPermissionsAvailable()) {
            return;
        }

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

        info.getWorlds().putAll(module.getPlayerGroups(player));
        EnjinMinecraftPlugin.getRankUpdatesConfiguration().getPlayerPerms().put(player.getName(), info);
    }

    public static void updatePlayersRanks(OfflinePlayer[] players) {
        for (OfflinePlayer player : players) {
            updatePlayerRanks1(player);
        }

        EnjinMinecraftPlugin.saveRankUpdatesConfiguration();
    }
}
