package com.enjin.sponge.sync;

import com.enjin.core.Enjin;
import com.enjin.core.InstructionHandler;
import com.enjin.rpc.mappings.mappings.plugin.ExecutedCommand;
import com.enjin.sponge.EnjinMinecraftPlugin;
import com.enjin.sponge.config.EMPConfig;
import com.enjin.sponge.listeners.ConnectionListener;
import com.google.common.base.Optional;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;

import java.io.File;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class SpongeInstructionHandler implements InstructionHandler {
    @Override
    public void addToWhitelist(String player) {
        Sponge.getCommandManager().process(Sponge.getServer().getConsole(), "whitelist add " + player);
    }

    @Override
    public void removeFromWhitelist(String player) {
        Sponge.getCommandManager().process(Sponge.getServer().getConsole(), "whitelist remove " + player);
    }

    @Override
    public void ban(String player) {
        Sponge.getCommandManager().process(Sponge.getServer().getConsole(), "ban " + player);
    }

    @Override
    public void pardon(String player) {
        Sponge.getCommandManager().process(Sponge.getServer().getConsole(), "pardon " + player);
    }


    @Override
    public void addToGroup(String player, String group, String world) {
        ConnectionListener.addGroup(player, group, world);
    }

    @Override
    public void removeFromGroup(String player, String group, String world) {
        ConnectionListener.removeGroup(player, group, world);
    }

    @Override
    public void execute(Long id, String command, Optional<Long> delay, Optional<Boolean> requireOnline, Optional<String> name, Optional<String> uuid) {
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

        Runnable runnable = () -> {
            java.util.Optional<Player> player = null;
            if (Sponge.getServer().getOnlineMode() && uuid.isPresent() && !uuid.get().isEmpty()) {
                Enjin.getLogger().debug("Searching for player by uuid...");
                String value = uuid.get().replaceAll("-", "");
                UUID u = null;
                if (value.length() == 32) {
                    BigInteger least = new BigInteger(value.substring(0, 16), 16);
                    BigInteger most = new BigInteger(value.substring(16, 32), 16);
                    u = new UUID(least.longValue(), most.longValue());
                } else {
                    Enjin.getLogger().debug("Player uuid " + value + " is invalid.");
                }

                if (u != null) {
                    player = Sponge.getServer().getPlayer(u);

                    if (player.isPresent()) {
                        Enjin.getLogger().debug("Player with uuid " + uuid.toString() + " has been found.");
                        Player p = player.get();
                        if (!p.hasPlayedBefore()) {
                            Enjin.getLogger().debug(p.getName() + " has not played before, skipping execute instruction...");
                            player = null;
                        }
                    }
                }
            }

            if ((player == null || !player.isPresent()) && name.isPresent() && !name.get().isEmpty()) {
                Enjin.getLogger().debug("Searching for player by name...");
                String n = name.get();
                if (n.length() < 16) {
                    player = Sponge.getServer().getPlayer(n);
                } else {
                    Enjin.getLogger().debug("Player name " + n + " is invalid.");
                }

                if (player.isPresent()) {
                    Enjin.getLogger().debug("Player with name " + n + " has been found.");
                    Player p = player.get();
                    if (!p.hasPlayedBefore()) {
                        Enjin.getLogger().debug(n + " has not played before, skipping execute instruction...");
                        player = null;
                    }
                } else {
                    Enjin.getLogger().debug("Player with name " + n + " could not be found.");
                }
            } else {
                if (player != null && player.isPresent()) {
                    Enjin.getLogger().debug("Player detected, skipping name check...");
                } else {
                    Enjin.getLogger().debug("Name is not present or empty.");
                }
            }

            if (requireOnline.isPresent() && requireOnline.get().booleanValue()) {
                Enjin.getLogger().debug("Player is required to be online, checking for player...");
                if (!player.isPresent() || !player.get().isOnline()) {
                    Enjin.getLogger().debug("The player is not online, skipping execute instruction...");
                    return;
                } else {
                    Enjin.getLogger().debug("The player is online.");
                }
            } else {
                Enjin.getLogger().debug("Player is not required to be online.");
            }

            if (player.isPresent()) {
                Enjin.getLogger().debug("Executing command \"" + command + "\" for " + player.get().getName());
                Sponge.getCommandManager().process(Sponge.getServer().getConsole(), command);
                EnjinMinecraftPlugin.getExecutedCommandsConfiguration().getExecutedCommands()
                        .add(new ExecutedCommand(Long.toString(id), command, Enjin.getLogger().getLastLine()));
                EnjinMinecraftPlugin.saveExecutedCommandsConfiguration();
            } else {
                Enjin.getLogger().debug("No matching player could be found.");
            }
        };

        if (!delay.isPresent() || delay.get() <= 0) {
            Sponge.getScheduler().createTaskBuilder().execute(runnable)
                    .submit(Enjin.getPlugin());
        } else {
            Sponge.getScheduler().createTaskBuilder().execute(runnable)
                    .delay(delay.get(), TimeUnit.SECONDS)
                    .submit(Enjin.getPlugin());
        }
    }

    @Override
    public void commandConfirmed(List<Long> executed) {
        for (ExecutedCommand command : new ArrayList<>(EnjinMinecraftPlugin.getExecutedCommandsConfiguration().getExecutedCommands())) {
            for (long id : executed) {
                Enjin.getLogger().debug("Confirming Command ID: " + id);
                if (Long.parseLong(command.getId()) == id) {
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
            config.update(new File(EnjinMinecraftPlugin.getInstance().getConfigDir(), "config.json"), update);
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
            java.util.Optional<Player> p = Sponge.getServer().getPlayer(player);
            if (p.isPresent()) {
                p.get().sendMessage(Text.of(message));
            }
        }
    }

    @Override
    public void version(String version) {
        // TODO
    }
}
