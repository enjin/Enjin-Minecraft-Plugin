package com.enjin.bukkit.sync;

import com.enjin.bukkit.config.EMPConfig;
import com.enjin.bukkit.listeners.ConnectionListener;
import com.enjin.bukkit.modules.impl.VaultModule;
import com.enjin.bukkit.tasks.EnjinUpdater;
import com.enjin.core.Enjin;
import com.enjin.core.InstructionHandler;
import com.enjin.bukkit.EnjinMinecraftPlugin;
import com.enjin.rpc.mappings.mappings.plugin.ExecutedCommand;
import com.google.common.base.Optional;
import com.google.common.collect.Lists;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.io.File;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

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
		VaultModule module = EnjinMinecraftPlugin.getInstance().getModuleManager().getModule(VaultModule.class);
        if (module == null || !module.isPermissionsAvailable()) {
            return;
        }

        OfflinePlayer p = Bukkit.getOfflinePlayer(player);

        if (p != null) {
            if (world == null || world.isEmpty() || world.equals("*")) {
                module.getPermission().playerAddGroup(null, p, group);
            } else {
                module.getPermission().playerAddGroup(world, p, group);
            }

            ConnectionListener.updatePlayerRanks(p);
        }
    }

    @Override
    public void removeFromGroup(String player, String group, String world) {
		VaultModule module = EnjinMinecraftPlugin.getInstance().getModuleManager().getModule(VaultModule.class);
        if (module == null || !module.isPermissionsAvailable()) {
            return;
        }

        OfflinePlayer p = Bukkit.getOfflinePlayer(player);

        if (p != null) {
            if (world == null || world.isEmpty() || world.equals("*")) {
                module.getPermission().playerRemoveGroup(null, p, group);
            } else {
                module.getPermission().playerRemoveGroup(world, p, group);
            }

            ConnectionListener.updatePlayerRanks(p);
        }
    }

    @Override
    public void execute(final Long id, final String command, final Optional<Long> delay, final Optional<Boolean> requireOnline, final Optional<String> name, final Optional<String> uuid) {
        if (id == null || id <= -1) {
            Enjin.getLogger().debug("Execute instruction has invalid id: " + id);
            return;
        }

        for (ExecutedCommand c : EnjinMinecraftPlugin.getExecutedCommandsConfiguration().getExecutedCommands()) {
            if (Long.parseLong(c.getId()) == id) {
                Enjin.getLogger().debug("Enjin has already processed the execution of instruction with id: " + id);
                return;
            }
        }

        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                OfflinePlayer player = null;
                if (uuid.isPresent() && !uuid.get().isEmpty()) {
                    String value = uuid.get().replaceAll("-", "");
                    UUID u = null;
                    if (value.length() == 32) {
                        BigInteger least = new BigInteger(value.substring(0, 16), 16);
                        BigInteger most = new BigInteger(value.substring(16, 32), 16);
                        u = new UUID(least.longValue(), most.longValue());
                        Enjin.getLogger().debug("Attempting to execute command for player uuid: " + u.toString());
                    } else {
                        Enjin.getLogger().debug("Received invalid uuid:" + value);
                    }

                    if (u != null) {
                        player = Bukkit.getPlayer(u);

                        if (player == null) {
                            player = Bukkit.getOfflinePlayer(u);
                            if (player != null && !player.hasPlayedBefore())
                                player = null;
                        }
                    }
                }

                if (player == null && name.isPresent() && !name.get().isEmpty()) {
                    String n = name.get();
                    player = Bukkit.getOfflinePlayer(n);
                    Enjin.getLogger().debug("Attempting to execute command for player name: " + n);

                    if (player == null) {
                        player = Bukkit.getOfflinePlayer(n);
                        if (player != null && !player.hasPlayedBefore())
                            player = null;
                    }
                }

                if (requireOnline.isPresent() && requireOnline.get().booleanValue()) {
                    if (player == null || !player.isOnline()) {
                        Enjin.getLogger().debug("The player is not online, skipping execute instruction...");
                        return;
                    }
                }

                if (player != null) {
                    EnjinMinecraftPlugin.dispatchConsoleCommand(command);
                    EnjinMinecraftPlugin.getExecutedCommandsConfiguration().getExecutedCommands()
                            .add(new ExecutedCommand(Long.toString(id), command, Enjin.getLogger().getLastLine()));
                    EnjinMinecraftPlugin.saveExecutedCommandsConfiguration();
                } else {
                    Enjin.getLogger().debug("A player could not be found in the cache for "
                            + (uuid.isPresent() ? "uuid: " + uuid.get() : name.isPresent() ? "name: " + name.get() : "n/a"));
                }
            }
        };

        if (EnjinMinecraftPlugin.getInstance().isEnabled()) {
            if (!delay.isPresent() || delay.get() <= 0) {
                Bukkit.getScheduler().scheduleSyncDelayedTask(EnjinMinecraftPlugin.getInstance(), runnable);
            } else {
                Bukkit.getScheduler().scheduleSyncDelayedTask(EnjinMinecraftPlugin.getInstance(), runnable, delay.get() * 20);
            }
        }
    }

    @Override
    public void commandConfirmed(List<Long> executed) {
        for (ExecutedCommand command : new ArrayList<>(EnjinMinecraftPlugin.getExecutedCommandsConfiguration().getExecutedCommands())) {
            for (long id : executed) {
                if (Long.parseLong(command.getId()) == id) {
                    Enjin.getLogger().debug("Confirming Command ID: " + id);
                    EnjinMinecraftPlugin.getExecutedCommandsConfiguration().getExecutedCommands().remove(command);
                }
            }
        }

        EnjinMinecraftPlugin.saveExecutedCommandsConfiguration();
    }

    @Override
    public void configUpdated(Object update) {
        EMPConfig config = Enjin.getConfiguration(EMPConfig.class);
        if (config != null) {
            config.update(new File(EnjinMinecraftPlugin.getInstance().getDataFolder(), "config.json"), update);
            EnjinMinecraftPlugin.getInstance().initConfig();
            EnjinMinecraftPlugin.saveConfiguration();
        }
    }

    @Override
    public void statusReceived(String status) {
        Enjin.getLogger().debug("Enjin Status: " + status);
    }

    @Override
    public void clearInGameCache(String player, int id, String price) {
        // TODO
    }

    @Override
    public void notify(List<String> players, String message, long time) {
        for (String player : players) {
            Player p = Bukkit.getPlayer(player);
            if (p != null) {
                p.sendMessage(message);
            }
        }
    }

    @Override
    public void version(String version) {
        EnjinMinecraftPlugin plugin = EnjinMinecraftPlugin.getInstance();

        if (Enjin.getConfiguration().isAutoUpdate() && !plugin.isHasUpdate() && !plugin.isUpdateFromCurseForge() &&!plugin.isUpdateFailed()) {
            plugin.setHasUpdate(true);
            Bukkit.getScheduler().runTaskAsynchronously(plugin, new EnjinUpdater(plugin.getServer().getUpdateFolder(), version, plugin));
        }
    }
}
