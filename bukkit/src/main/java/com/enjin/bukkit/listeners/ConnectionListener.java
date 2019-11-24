package com.enjin.bukkit.listeners;

import com.enjin.bukkit.EnjinMinecraftPlugin;
import com.enjin.bukkit.modules.impl.VaultModule;
import com.enjin.bukkit.util.PermissionsUtil;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public class ConnectionListener implements Listener {
    @Getter
    private static ConnectionListener   instance;
    private        EnjinMinecraftPlugin plugin;

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

        VaultModule module = plugin.getModuleManager().getModule(VaultModule.class);
        if (module != null && module.isEconomyAvailable() && !module.isEconomyUpToDate() && PermissionsUtil.hasPermission(
                p,
                "enjin.notify.econoutdated")) {
            p.sendMessage(ChatColor.RED + "[EnjinMinecraftPlugin] " + module.getEconomy()
                                                                            .getName() + " doesn't have UUID support, please update. Using Vault compatibility mode.");
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerQuit(PlayerQuitEvent e) {
        Player p = e.getPlayer();
        updatePlayerRanks(p);
    }

    public static void updatePlayerRanks(OfflinePlayer player) {
        updatePlayerRanks1(player);
    }

    public static void updatePlayerRanks1(OfflinePlayer player) {
        if (player == null || player.getName() == null)
            return;

        EnjinMinecraftPlugin plugin = EnjinMinecraftPlugin.getInstance();
        VaultModule module = plugin.getModuleManager().getModule(VaultModule.class);
        if (module == null || !module.isPermissionsAvailable())
            return;

        Bukkit.getScheduler().runTaskAsynchronously(EnjinMinecraftPlugin.getInstance(), () -> {
            Map<String, List<String>> playerGroups = module.getPlayerGroups(player);

            try {
                plugin.db().deleteGroups(player.getName());

                for (Map.Entry<String, List<String>> entry : playerGroups.entrySet()) {
                    plugin.db().addGroups(player.getUniqueId(), player.getName(),
                            entry.getKey(), entry.getValue());
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }

    public static void updatePlayersRanks(OfflinePlayer[] players) {
        for (OfflinePlayer player : players) {
            updatePlayerRanks1(player);
        }
    }
}
