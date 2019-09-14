package com.enjin.bukkit.sync;

import com.enjin.bukkit.EnjinMinecraftPlugin;
import com.enjin.bukkit.config.EMPConfig;
import com.enjin.bukkit.events.PostSyncEvent;
import com.enjin.bukkit.events.PreSyncEvent;
import com.enjin.bukkit.modules.impl.VaultModule;
import com.enjin.bukkit.modules.impl.VotifierModule;
import com.enjin.bukkit.storage.StoredCommand;
import com.enjin.bukkit.sync.data.AddPlayerGroupInstruction;
import com.enjin.bukkit.sync.data.AddWhitelistPlayerInstruction;
import com.enjin.bukkit.sync.data.BanPlayersInstruction;
import com.enjin.bukkit.sync.data.CommandsReceivedInstruction;
import com.enjin.bukkit.sync.data.ExecuteCommandInstruction;
import com.enjin.bukkit.sync.data.NewerVersionInstruction;
import com.enjin.bukkit.sync.data.NotificationsInstruction;
import com.enjin.bukkit.sync.data.PardonPlayersInstruction;
import com.enjin.bukkit.sync.data.RemoteConfigUpdateInstruction;
import com.enjin.bukkit.sync.data.RemovePlayerGroupInstruction;
import com.enjin.bukkit.sync.data.RemoveWhitelistPlayerInstruction;
import com.enjin.bukkit.tasks.TPSMonitor;
import com.enjin.core.Enjin;
import com.enjin.core.EnjinServices;
import com.enjin.core.util.StringUtils;
import com.enjin.rpc.mappings.mappings.general.RPCData;
import com.enjin.rpc.mappings.mappings.plugin.Instruction;
import com.enjin.rpc.mappings.mappings.plugin.PlayerGroupInfo;
import com.enjin.rpc.mappings.mappings.plugin.PlayerInfo;
import com.enjin.rpc.mappings.mappings.plugin.SyncResponse;
import com.enjin.rpc.mappings.mappings.plugin.data.ExecuteData;
import com.enjin.rpc.mappings.mappings.plugin.data.NotificationData;
import com.enjin.rpc.mappings.mappings.plugin.data.PlayerGroupUpdateData;
import com.enjin.rpc.mappings.services.PluginService;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class RPCPacketManager implements Runnable {

    private static final Gson GSON = new GsonBuilder().serializeNulls()
            .create();

    private final EnjinMinecraftPlugin plugin;

    private boolean firstRun = true;
    private int elapsed = 0;

    public RPCPacketManager(EnjinMinecraftPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void run() {
        try {
            Enjin.getLogger().debug("Syncing with Enjin services...");
            sync();
        } catch (Exception e) {
            Enjin.getLogger().warning("An error occurred while syncing with Enjin services...");
            Enjin.getLogger().log(e);
        }
    }

    @SuppressWarnings("unchecked")
    private void sync() {
        int syncDelay = Enjin.getConfiguration().getSyncDelay();
        if (!this.firstRun && syncDelay > 0) {
            if (Bukkit.getOnlinePlayers().isEmpty()) {
                if (++this.elapsed < syncDelay) {
                    Enjin.getLogger()
                            .debug("No players online, server will sync after 10 minutes have elapsed. Minutes remaining: "
                                    + (syncDelay - this.elapsed));
                    return;
                }
            }
        }

        List<StoredCommand> executedCommands = new ArrayList<>();

        try {
            plugin.db().commit();
            executedCommands.addAll(plugin.db().getExecutedCommands());
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        Enjin.getLogger().debug("Constructing payload...");
        HashMap<String, Object> status = new HashMap<>();
        status.put("java_version", System.getProperty("java.version"));
        status.put("mc_version", this.plugin.getMcVersion());
        status.put("plugins", getPlugins());
        status.put("hasranks", isPermissionsAvailable());
        status.put("pluginversion", this.plugin.getDescription().getVersion());
        status.put("worlds", getWorlds());
        status.put("groups", getGroups());
        status.put("maxplayers", getMaxPlayers());
        status.put("players", getOnlineCount());
        status.put("playerlist", getOnlinePlayers());
        status.put("playergroups", getPlayerGroups());
        status.put("tps", TPSMonitor.getInstance().getLastTPSMeasurement());
        status.put("executed_commands", executedCommands.stream()
                .map(StoredCommand::toMap)
                .collect(Collectors.toList()));
        if (Enjin.getConfiguration(EMPConfig.class).getEnabledComponents().isVoteListener()) {
            status.put("votifier", getVotes());
        }

        Bukkit.getPluginManager().callEvent(new PreSyncEvent(status));

        Enjin.getLogger().debug("Fetching plugin service...");
        PluginService service = EnjinServices.getService(PluginService.class);
        Enjin.getLogger().debug("Syncing...");

        RPCData<SyncResponse> data = null;

        try {
            service.sync(status);
        } catch (Exception ex) {
            Enjin.getLogger().log(ex);
            Enjin.getLogger().debug(GSON.toJson(status));
        }

        Enjin.getLogger().debug("Sync complete...");

        if (data == null) {
            Bukkit.getPluginManager().callEvent(new PostSyncEvent(false, null));
            return;
        }

        if (data.getError() != null) {
            this.plugin.getLogger().warning(data.getError().getMessage());
            Bukkit.getPluginManager().callEvent(new PostSyncEvent(false, data));
        } else {
            SyncResponse result = data.getResult();
            if (result != null && result.getStatus().equalsIgnoreCase("ok")) {
                Bukkit.getPluginManager().callEvent(new PostSyncEvent(true, data));

                for (Instruction instruction : result.getInstructions()) {
                    switch (instruction.getCode()) {
                        case ADD_PLAYER_GROUP:
                            AddPlayerGroupInstruction.handle((PlayerGroupUpdateData) instruction.getData());
                            break;
                        case REMOVE_PLAYER_GROUP:
                            RemovePlayerGroupInstruction.handle((PlayerGroupUpdateData) instruction.getData());
                            break;
                        case EXECUTE:
                            ExecuteCommandInstruction.handle((ExecuteData) instruction.getData());
                            break;
                        case CONFIRMED_COMMANDS:
                            CommandsReceivedInstruction.handle((ArrayList<Long>) instruction.getData());
                            break;
                        case CONFIG:
                            RemoteConfigUpdateInstruction.handle((Map<String, Object>) instruction.getData());
                            break;
                        case ADD_PLAYER_WHITELIST:
                            AddWhitelistPlayerInstruction.handle((String) instruction.getData());
                            break;
                        case REMOVE_PLAYER_WHITELIST:
                            RemoveWhitelistPlayerInstruction.handle((String) instruction.getData());
                            break;
                        case RESPONSE_STATUS:
                            Enjin.getPlugin().getInstructionHandler().statusReceived((String) instruction.getData());
                            break;
                        case BAN_PLAYER:
                            BanPlayersInstruction.handle((String) instruction.getData());
                            break;
                        case UNBAN_PLAYER:
                            PardonPlayersInstruction.handle((String) instruction.getData());
                            break;
                        case NOTIFICATIONS:
                            NotificationsInstruction.handle((NotificationData) instruction.getData());
                            break;
                        case PLUGIN_VERSION:
                            NewerVersionInstruction.handle((String) instruction.getData());
                            break;
                        default:
                            break;
                    }
                }

                try {
                    plugin.db().commit();
                    plugin.db().backup();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            } else {
                Enjin.getLogger()
                        .debug("Did not receive \"ok\" status. Status: " + (result == null ? "n/a" : result.getStatus()));
                Bukkit.getPluginManager().callEvent(new PostSyncEvent(false, data));
            }
        }

        this.firstRun = false;
        this.elapsed = 0;
    }

    private List<String> getPlugins() {
        List<String> plugins = new ArrayList<>();
        for (Plugin plugin : Bukkit.getPluginManager().getPlugins()) {
            plugins.add(plugin.getName());
        }
        return plugins;
    }

    private List<String> getWorlds() {
        List<String> worlds = new ArrayList<>();
        for (World plugin : Bukkit.getWorlds()) {
            worlds.add(plugin.getName());
        }
        return worlds;
    }

    private List<String> getGroups() {
        List<String> groups = new ArrayList<>();

        VaultModule module = plugin.getModuleManager().getModule(VaultModule.class);
        if (module != null && module.isPermissionsAvailable()) {
            try {
                groups.addAll(Arrays.asList(module.getPermission().getGroups()));
            } catch (Exception e) {
                Enjin.getLogger().warning("Exception thrown by Vault permissions implementation. " +
                        "Please ensure Vault and your permissions plugin are up-to-date.");
                Enjin.getLogger().debug("Vault Exception: \n" +
                        StringUtils.throwableToString(e));
            }
        }

        return groups;
    }

    private int getMaxPlayers() {
        return Bukkit.getMaxPlayers();
    }

    private int getOnlineCount() {
        return Bukkit.getOnlinePlayers().size();
    }

    @SuppressWarnings("deprecation")
    private List<PlayerInfo> getOnlinePlayers() {
        List<PlayerInfo> infos = new ArrayList<>();
        for (Player player : Bukkit.getOnlinePlayers()) {
            infos.add(new PlayerInfo(player.getName(),
                    Enjin.getApi().getVanishState(player.getUniqueId()),
                    player.getUniqueId()));
        }
        return infos;
    }

    private Map<String, PlayerGroupInfo> getPlayerGroups() {
        Map<String, PlayerGroupInfo> result = null;

        try {
            result = plugin.db().getGroups();

            for (Map.Entry<String, PlayerGroupInfo> entry : result.entrySet()) {
                plugin.db().deleteGroups(entry.getKey());
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return result;
    }

    private Map<String, List<Object[]>> getVotes() {
        Map<String, List<Object[]>> votes = null;
        VotifierModule module = this.plugin.getModuleManager().getModule(VotifierModule.class);
        if (module != null && !module.getPlayerVotes().isEmpty()) {
            votes = new HashMap<>(module.getPlayerVotes());
            module.getPlayerVotes().clear();
        }
        return votes;
    }

    //    private String getStats() {
    //        return new WriteStats(plugin).getStatsJSON();
    //    }

    private boolean isPermissionsAvailable() {
        VaultModule module = this.plugin.getModuleManager().getModule(VaultModule.class);
        return module != null && module.isPermissionsAvailable();
    }
}
