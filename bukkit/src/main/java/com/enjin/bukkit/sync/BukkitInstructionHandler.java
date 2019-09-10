package com.enjin.bukkit.sync;

import com.enjin.bukkit.EnjinMinecraftPlugin;
import com.enjin.bukkit.config.EMPConfig;
import com.enjin.bukkit.listeners.ConnectionListener;
import com.enjin.bukkit.modules.impl.VaultModule;
import com.enjin.bukkit.storage.StoredCommand;
import com.enjin.bukkit.util.PlayerUtil;
import com.enjin.core.Enjin;
import com.enjin.core.InstructionHandler;
import com.google.common.base.Optional;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.io.File;
import java.math.BigInteger;
import java.sql.SQLException;
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
    public void execute(final Long id,
                        final String command,
                        final Optional<Long> delay,
                        final Optional<Boolean> requireOnline,
                        final Optional<String> name,
                        final Optional<String> uuid) {
        if (id == null || id < 0)
            return;

        Optional<StoredCommand> commandRecord = Optional.absent();

        try {
            commandRecord = Optional.fromNullable(EnjinMinecraftPlugin.getInstance().db().getCommand(id));
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        if (commandRecord.isPresent())
            return;

        Optional<? extends OfflinePlayer> player = Optional.absent();
        Optional<UUID> playerUuid = Optional.absent();
        if (Bukkit.getOnlineMode() && uuid.isPresent()) {
            String val = uuid.get().replaceAll("-", "");
            if (val.length() == 32) {
                BigInteger least = new BigInteger(val.substring(0, 16), 16);
                BigInteger most = new BigInteger(val.substring(16, 32), 16);
                playerUuid = Optional.of(new UUID(least.longValue(), most.longValue()));
            }

            if (playerUuid.isPresent()) {
                player = PlayerUtil.getPlayer(playerUuid.get());
                if (!player.isPresent())
                    player = PlayerUtil.getOfflinePlayer(playerUuid.get(), true);
            }
        }

        if (!player.isPresent() && name.isPresent()) {
            String val = name.get();

            if (val.length() <= 16) {
                player = PlayerUtil.getPlayer(val);

                if (!player.isPresent())
                    PlayerUtil.getOfflinePlayer(val,true);
            }
        }

        if (requireOnline.or(false)) {
            if (!player.isPresent())
                return;

            if (!player.get().isOnline())
                return;
        }

        StoredCommand toExecute = new StoredCommand(id, command, delay, requireOnline, name, playerUuid);

        try {
            EnjinMinecraftPlugin.getInstance().db().insertCommand(toExecute);
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        EnjinMinecraftPlugin.getInstance().getCommandExecutor().register(toExecute);
    }

    @Override
    public void commandConfirmed(List<Long> executed) {
        for (Long id : executed) {
            try {
                EnjinMinecraftPlugin.getInstance().db().deleteCommand(id);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        try {
            EnjinMinecraftPlugin.getInstance().db().commit();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void configUpdated(Object update) {
        EMPConfig config = Enjin.getConfiguration(EMPConfig.class);
        if (config != null) {
            config.update(new File(EnjinMinecraftPlugin.getInstance().getDataFolder(), "config.json"), update);
            EnjinMinecraftPlugin.getInstance().initConfig();
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
