package com.enjin.bukkit.sync;

import com.enjin.bukkit.EnjinMinecraftPlugin;
import com.enjin.bukkit.storage.StoredCommand;
import com.enjin.bukkit.util.PlayerUtil;
import com.enjin.bukkit.util.TimeUtil;
import com.enjin.core.Enjin;
import com.google.common.base.Optional;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CommandExecutor extends BukkitRunnable {

    private Map<Long, StoredCommand> commandMap = new HashMap<>();
    private List<StoredCommand> commands = new ArrayList<>();
    private EnjinMinecraftPlugin plugin;

    public CommandExecutor(EnjinMinecraftPlugin plugin) {
        this.plugin = plugin;

        try {
            plugin.db().getPendingCommands().forEach(c -> register(c));
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        for (int i = 0; i < commands.size(); i++) {
            StoredCommand command = commands.get(i);

            if (command.getDelay().isPresent()) {
                long now = TimeUtil.utcNowSeconds();
                long delayEndsAfter = command.getCreatedAt() + command.getDelay().get();

                if (now <= delayEndsAfter)
                    continue;
            }

            if (command.getRequireOnline().or(false)) {
                Optional<Player> player = Optional.absent();
                if (Bukkit.getOnlineMode() && command.getPlayerUuid().isPresent())
                    player = PlayerUtil.getPlayer(command.getPlayerUuid().get());
                else if (command.getPlayerName().isPresent())
                    player = PlayerUtil.getPlayer(command.getPlayerName().get());

                if (!player.isPresent())
                    continue;
            }

            remove(command);

            EnjinMinecraftPlugin.dispatchConsoleCommand(command.getCommand(), () -> {
                try {
                    plugin.db().setCommandAsExecuted(command.getId(),
                            command.generateHash(),
                            Enjin.getLogger().getLastLine());
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }, true);

            i--;
        }
    }

    public void register(StoredCommand command) {
        commands.add(command);
        commandMap.put(command.getId(), command);
    }

    public void remove(StoredCommand command) {
        commands.remove(command);
        commandMap.remove(command.getId());
    }

}
