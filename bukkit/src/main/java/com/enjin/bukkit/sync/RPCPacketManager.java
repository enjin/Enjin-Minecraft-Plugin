package com.enjin.bukkit.sync;

import com.enjin.bukkit.config.EMPConfig;
import com.enjin.bukkit.config.RankUpdatesConfig;
import com.enjin.bukkit.modules.impl.VaultModule;
import com.enjin.bukkit.modules.impl.VotifierModule;
import com.enjin.bukkit.stats.WriteStats;
import com.enjin.bukkit.sync.data.*;
import com.enjin.bukkit.tasks.TPSMonitor;
import com.enjin.core.Enjin;
import com.enjin.core.EnjinServices;
import com.enjin.bukkit.EnjinMinecraftPlugin;
import com.enjin.rpc.mappings.mappings.general.RPCData;
import com.enjin.rpc.mappings.mappings.plugin.*;
import com.enjin.rpc.mappings.mappings.plugin.data.ExecuteData;
import com.enjin.rpc.mappings.mappings.plugin.data.NotificationData;
import com.enjin.rpc.mappings.mappings.plugin.data.PlayerGroupUpdateData;
import com.enjin.rpc.mappings.services.PluginService;
import java.util.logging.Logger;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.*;
import java.util.concurrent.TimeUnit;

public class RPCPacketManager implements Runnable {
    private EnjinMinecraftPlugin plugin;
    private long nextStatUpdate = System.currentTimeMillis();

    public RPCPacketManager(EnjinMinecraftPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void run() {
        try {
            Enjin.getLogger().debug("Syncing with Enjin services...");
            sync();
        } catch (Exception e) {
            Enjin.getLogger().log("An error occured while syncing with Enjin services...", e);
        }
    }

    private void sync() {
        String stats = null;
        if (Enjin.getConfiguration(EMPConfig.class).isCollectPlayerStats() && System.currentTimeMillis() > nextStatUpdate) {
            Enjin.getLogger().debug("Collecting player stats...");
            stats = getStats();
            nextStatUpdate = System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(5);
            Enjin.getLogger().debug("Player stats collected!");
        }

        Enjin.getLogger().debug("Constructing payload...");
        Status status = new Status(System.getProperty("java.version"),
                plugin.getMcVersion(),
                getPlugins(),
                isPermissionsAvailable(),
                plugin.getDescription().getVersion(),
                getWorlds(),
                getGroups(),
                getMaxPlayers(),
                getOnlineCount(),
                getOnlinePlayers(),
                getPlayerGroups(),
                TPSMonitor.getInstance().getLastTPSMeasurement(),
                EnjinMinecraftPlugin.getExecutedCommandsConfiguration().getExecutedCommands(),
                getVotes(),
                stats);

        Enjin.getLogger().debug("Fetching plugin service...");
        PluginService service = EnjinServices.getService(PluginService.class);
        Enjin.getLogger().debug("Syncing...");
        RPCData<SyncResponse> data = service.sync(status);
        Enjin.getLogger().debug("Sync complete...");

        if (data == null) {
            Enjin.getLogger().debug("Data is null while requesting sync update from Plugin.sync.");
            return;
        }

        if (data.getError() != null) {
            plugin.getLogger().warning(data.getError().getMessage());
        } else {
            SyncResponse result = data.getResult();
            if (result != null && result.getStatus().equalsIgnoreCase("ok")) {
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
                        case EXECUTE_AS:
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
                        case CLEAR_INGAME_CACHE:
                            break;
                        case NOTIFICATIONS:
                            NotificationsInstruction.handle((NotificationData) instruction.getData());
                            break;
                        case PLUGIN_VERSION:
                            NewerVersionInstruction.handle((String) instruction.getData());
                            break;
                        default:
                    }
                }
            } else {
                Enjin.getLogger().debug("Did not receive \"ok\" status. Status: " + (result == null ? "n/a" : result.getStatus()));
            }
        }
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
            groups.addAll(Arrays.asList(module.getPermission().getGroups()));
        }

        return groups;
    }

    private int getMaxPlayers() {
        return Bukkit.getMaxPlayers();
    }

    private int getOnlineCount() {
        return Bukkit.getOnlinePlayers().size();
    }

    private List<PlayerInfo> getOnlinePlayers() {
        List<PlayerInfo> infos = new ArrayList<>();
        for (Player player : Bukkit.getOnlinePlayers()) {
            infos.add(new PlayerInfo(player.getName(), Enjin.getApi().getVanishState(player.getUniqueId()), player.getUniqueId()));
        }
        return infos;
    }

    private Map<String, PlayerGroupInfo> getPlayerGroups() {
        RankUpdatesConfig config = EnjinMinecraftPlugin.getRankUpdatesConfiguration();

        if (config == null) {
            Enjin.getLogger().warning("Rank updates configuration did not load properly.");
            return null;
        }

        Map<String, PlayerGroupInfo> groups = config.getPlayerPerms();
        Map<String, PlayerGroupInfo> update = new HashMap<>();

        int index = 0;
        for (String player : new HashSet<>(groups.keySet())) {
            if (index >= 500) {
                break;
            }

            update.put(player, groups.get(player));
        }

        for (Map.Entry<String, PlayerGroupInfo> entry : update.entrySet()) {
            groups.remove(entry.getKey());
        }

        EnjinMinecraftPlugin.saveRankUpdatesConfiguration();
        return update;
    }

    private Map<String, List<Object[]>> getVotes() {
        Map<String, List<Object[]>> votes = null;
        VotifierModule module = plugin.getModuleManager().getModule(VotifierModule.class);
        if (module != null && !module.getPlayerVotes().isEmpty()) {
            votes = new HashMap<>(module.getPlayerVotes());
            module.getPlayerVotes().clear();
        }
        return votes;
    }

    private String getStats() {
        return new WriteStats(plugin).getStatsJSON();
    }

    private boolean isPermissionsAvailable() {
        VaultModule module = plugin.getModuleManager().getModule(VaultModule.class);
        return module == null ? false : module.isPermissionsAvailable();
    }
}
