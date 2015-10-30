package com.enjin.bukkit.listeners.perm;

import com.enjin.bukkit.EnjinMinecraftPlugin;
import com.enjin.bukkit.listeners.ConnectionListener;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.tyrannyofheaven.bukkit.zPermissions.ZPermissionsRankChangeEvent;

public class ZPermissionsListener implements Listener {
    private EnjinMinecraftPlugin plugin;

    public ZPermissionsListener(EnjinMinecraftPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onRankChange(ZPermissionsRankChangeEvent event) {
        update(event.getPlayerName());
    }

    @EventHandler
    public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event) {
        String[] args = event.getMessage().replaceFirst("/", "").split(" ");
        if (args.length >= 5 && (args[0].equalsIgnoreCase("perm") || args[0].equalsIgnoreCase("permissions"))) {
            if (args[1].equalsIgnoreCase("player")) {
                String name = args[2];

                if (args[3].equalsIgnoreCase("setgroup") || args[3].equalsIgnoreCase("group")) {
                    if (args[4].equalsIgnoreCase("-A") || args[4].equalsIgnoreCase("--add") || args[4].equalsIgnoreCase("--add-no-reset")) {
                        if (args.length < 6) {
                            return;
                        } else {
                            update(name);
                        }
                    } else {
                        update(name);
                    }
                } else if (args[3].equalsIgnoreCase("removegroup") || args[3].equalsIgnoreCase("rmgroup") || args[3].equalsIgnoreCase("remove") || args[3].equalsIgnoreCase("rm")) {
                    update(name);
                }
            }
        }
    }

    public void update(String name) {
        Player player = Bukkit.getPlayer(name);

        if (player != null) {
            update(player);
        } else {
            OfflinePlayer offline = Bukkit.getOfflinePlayer(name);
            if (offline == null) {
                return;
            } else {
                update(offline);
            }
        }

        plugin.debug(player.getName() + " just got a rank change... processing...");
    }

    public void update(OfflinePlayer player) {
        ConnectionListener.getInstance().updatePlayerRanks(player.getName(), player.getUniqueId().toString());
    }
}