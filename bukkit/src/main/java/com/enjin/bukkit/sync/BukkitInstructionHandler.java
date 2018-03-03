package com.enjin.bukkit.sync;

import com.enjin.bukkit.EnjinMinecraftPlugin;
import com.enjin.bukkit.config.EMPConfig;
import com.enjin.bukkit.listeners.ConnectionListener;
import com.enjin.bukkit.modules.impl.VaultModule;
import com.enjin.bukkit.tasks.EnjinUpdater;
import com.enjin.bukkit.util.PlayerUtil;
import com.enjin.core.Enjin;
import com.enjin.core.InstructionHandler;
import com.enjin.rpc.mappings.mappings.plugin.ExecutedCommand;
import com.google.common.base.Optional;
import java.io.File;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

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

        OfflinePlayer player = null;
        if (Bukkit.getOnlineMode() && uuid.isPresent() && !uuid.get().isEmpty()) {
            String value = uuid.get().replaceAll("-", "");
            UUID u = null;
            if (value.length() == 32) {
                BigInteger least = new BigInteger(value.substring(0, 16), 16);
                BigInteger most = new BigInteger(value.substring(16, 32), 16);
                u = new UUID(least.longValue(), most.longValue());
                Enjin.getLogger().debug("UUID Detected: " + u.toString());
            } else {
                Enjin.getLogger().debug("Invalid UUID: " + value);
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
            if (n.length() <= 16) {
                player = Bukkit.getPlayer(n);
                Enjin.getLogger().debug("Name Detected: " + n);
            } else {
                Enjin.getLogger().debug("Invalid Name: " + n);
            }

            if (player == null) {
                player = PlayerUtil.getOfflinePlayer(n, true);
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

        if (EnjinMinecraftPlugin.getInstance().getPendingCommands().contains(id)) {
            return;
        }

        final OfflinePlayer pl = player;
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                if (requireOnline.isPresent() && requireOnline.get()) {
                    if (pl == null || !pl.hasPlayedBefore() || !pl.isOnline()) {
                        return;
                    }
                }

                EnjinMinecraftPlugin plugin = EnjinMinecraftPlugin.getInstance();
                if (!plugin.getExecutedCommands().contains(id)) {
                    if (id > 0) {
                        plugin.getExecutedCommands().add(id);
                        plugin.getPendingCommands().remove(id);
                        EnjinMinecraftPlugin.getExecutedCommandsConfiguration().getExecutedCommands()
                                .add(new ExecutedCommand(Long.toString(id), command, Enjin.getLogger().getLastLine()));
                        EnjinMinecraftPlugin.saveExecutedCommandsConfiguration();
                    }

                    EnjinMinecraftPlugin.dispatchConsoleCommand(command);
                }
            }
        };

        if (EnjinMinecraftPlugin.getInstance().isEnabled()) {
            if (!EnjinMinecraftPlugin.getInstance().getPendingCommands().contains(id)) {
                if (id > 0) {
                    EnjinMinecraftPlugin.getInstance().getPendingCommands().add(id);
                }

                if (!delay.isPresent() || delay.get() <= 0) {
                    Bukkit.getScheduler().scheduleSyncDelayedTask(EnjinMinecraftPlugin.getInstance(), runnable);
                } else {
                    Bukkit.getScheduler().scheduleSyncDelayedTask(EnjinMinecraftPlugin.getInstance(), runnable, delay.get() * 20);
                }
            }
        }
    }

    @Override
    public void commandConfirmed(List<Long> executed) {
        for (ExecutedCommand command : new ArrayList<>(EnjinMinecraftPlugin.getExecutedCommandsConfiguration().getExecutedCommands())) {
            for (Long id : executed) {
                if (id == null) {
                    Enjin.getLogger().debug("Null executed command id detected... This should not happen.");
                    continue;
                }

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
    }
}
