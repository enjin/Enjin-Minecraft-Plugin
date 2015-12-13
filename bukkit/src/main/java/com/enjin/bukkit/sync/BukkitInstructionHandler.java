package com.enjin.bukkit.sync;

import com.enjin.bukkit.config.EnjinConfig;
import com.enjin.bukkit.listeners.ConnectionListener;
import com.enjin.bukkit.managers.VaultManager;
import com.enjin.bukkit.util.Log;
import com.enjin.core.Enjin;
import com.enjin.core.InstructionHandler;
import com.enjin.bukkit.EnjinMinecraftPlugin;
import com.enjin.rpc.mappings.mappings.plugin.ExecutedCommand;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class BukkitInstructionHandler implements InstructionHandler {
    @Override
    public void addToWhitelist(String player) {
        EnjinMinecraftPlugin.dispatchConsoleCommand("whitelist add " + player);
    }

    @Override
    public void removeFromWhitelist(String player) {
        EnjinMinecraftPlugin.dispatchConsoleCommand("whitelist remove " + player);
    }

    @Override
    public void ban(String player) {
        EnjinMinecraftPlugin.dispatchConsoleCommand("ban " + player);
    }

    @Override
    public void pardon(String player) {
        EnjinMinecraftPlugin.dispatchConsoleCommand("pardon " + player);
    }

    @Override
    public void addToGroup(String player, String group, String world) {
        if (!VaultManager.isVaultEnabled() || VaultManager.getPermission() == null) {
            return;
        }

        OfflinePlayer p = Bukkit.getOfflinePlayer(player);

        if (p != null) {
            if (world == null || world.isEmpty() || world.equals("*")) {
                VaultManager.getPermission().playerAddGroup(null, p, group);
            } else {
                VaultManager.getPermission().playerAddGroup(world, p, group);
            }

            ConnectionListener.updatePlayerRanks(p);
        }
    }

    @Override
    public void removeFromGroup(String player, String group, String world) {
        if (!VaultManager.isVaultEnabled() || VaultManager.getPermission() == null) {
            return;
        }

        OfflinePlayer p = Bukkit.getOfflinePlayer(player);

        if (p != null) {
            if (world == null || world.isEmpty() || world.equals("*")) {
                VaultManager.getPermission().playerRemoveGroup(null, p, group);
            } else {
                VaultManager.getPermission().playerRemoveGroup(world, p, group);
            }

            ConnectionListener.updatePlayerRanks(p);
        }
    }

    @Override
    public void execute(long id, String command, long delay) {
        for (ExecutedCommand c : EnjinMinecraftPlugin.getExecutedCommandsConfiguration().getExecutedCommands()) {
            if (Long.parseLong(c.getId()) == id) {
                return;
            }
        }

        Runnable runnable = () -> {
            EnjinMinecraftPlugin.dispatchConsoleCommand(command);
            EnjinMinecraftPlugin.getExecutedCommandsConfiguration().getExecutedCommands().add(new ExecutedCommand(Long.toString(id), command, Log.getLastLine()));
            EnjinMinecraftPlugin.saveExecutedCommandsConfiguration();
        };

        if (delay <= 0) {
            Bukkit.getScheduler().scheduleSyncDelayedTask(EnjinMinecraftPlugin.getInstance(), runnable);
        } else {
            Bukkit.getScheduler().scheduleSyncDelayedTask(EnjinMinecraftPlugin.getInstance(), runnable, delay * 20);
        }
    }

    @Override
    public void commandConfirmed(List<Long> executed) {
        for (long id : executed) {
            Enjin.getPlugin().debug("Confirming Command ID: " + id);
            new ArrayList<>(EnjinMinecraftPlugin.getExecutedCommandsConfiguration().getExecutedCommands()).stream().filter(command -> Long.parseLong(command.getId()) == id).forEach(command -> EnjinMinecraftPlugin.getExecutedCommandsConfiguration().getExecutedCommands().remove(command));
            EnjinMinecraftPlugin.saveExecutedCommandsConfiguration();
        }
    }

    @Override
    public void configUpdated(Object update) {
        EnjinConfig config = EnjinMinecraftPlugin.getConfiguration();
        if (config != null) {
            config.update(new File(EnjinMinecraftPlugin.getInstance().getDataFolder(), "config.json"), update);
            EnjinMinecraftPlugin.getInstance().initConfig();
            EnjinMinecraftPlugin.saveConfiguration();
        }
    }

    @Override
    public void statusReceived(String status) {
        Enjin.getPlugin().debug("Enjin Status: " + status);
    }

    @Override
    public void clearInGameCache(String player, int id, String price) {
        // TODO
    }

    @Override
    public void notify(List<String> players, String message, long time) {
        players.stream().filter(p -> Bukkit.getPlayer(p) != null).forEach(p -> Bukkit.getPlayer(p).sendMessage(message));
    }

    @Override
    public void version(String version) {
        // TODO
    }
}
