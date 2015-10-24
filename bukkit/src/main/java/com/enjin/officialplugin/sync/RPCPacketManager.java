package com.enjin.officialplugin.sync;

import com.enjin.core.Enjin;
import com.enjin.core.EnjinServices;
import com.enjin.officialplugin.EnjinMinecraftPlugin;
import com.enjin.officialplugin.sync.data.*;
import com.enjin.rpc.mappings.mappings.general.RPCData;
import com.enjin.rpc.mappings.mappings.plugin.*;
import com.enjin.rpc.mappings.mappings.plugin.data.ExecuteData;
import com.enjin.rpc.mappings.mappings.plugin.data.NotificationData;
import com.enjin.rpc.mappings.mappings.plugin.data.PlayerGroupUpdateData;
import com.enjin.rpc.mappings.services.PluginService;
import com.google.common.collect.Lists;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.World;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class RPCPacketManager implements Runnable {
    @Getter
    private static List<ExecutedCommand> executedCommands = Lists.newArrayList();

    private EnjinMinecraftPlugin plugin;

    public RPCPacketManager(EnjinMinecraftPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void run() {
        Status status = new Status(false,
                plugin.getDescription().getVersion(),
                getWorlds(),
                getGroups(),
                getMaxPlayers(),
                getOnlineCount(),
                getOnlinePlayers(),
                null,
                executedCommands,
                null);

        PluginService service = EnjinServices.getService(PluginService.class);
        RPCData<SyncResponse> data = service.sync(plugin.getAuthKey(), status);

        if (data == null) {
            Enjin.getPlugin().debug("Data is null while requesting sync update from Plugin.sync.");
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
                            Packet10AddPlayerGroup.handle((PlayerGroupUpdateData) instruction.getData());
                            break;
                        case REMOVE_PLAYER_GROUP:
                            Packet11RemovePlayerGroup.handle((PlayerGroupUpdateData) instruction.getData());
                            break;
                        case EXECUTE:
                            Packet12ExecuteCommand.handle((ExecuteData) instruction.getData());
                            break;
                        case EXECUTE_AS:
                            break;
                        case CONFIRMED_COMMANDS:
                            Packet1ECommandsReceived.handle((ArrayList<Long>) instruction.getData());
                            break;
                        case CONFIG:
                            Packet15RemoteConfigUpdate.handle((Map<String, Object>) instruction.getData());
                            break;
                        case ADD_PLAYER_WHITELIST:
                            Packet17AddWhitelistPlayers.handle((String) instruction.getData());
                            break;
                        case REMOVE_PLAYER_WHITELIST:
                            Packet18RemovePlayersFromWhitelist.handle((String) instruction.getData());
                            break;
                        case RESPONSE_STATUS:
                            Enjin.getPlugin().getInstructionHandler().statusReceived((String) instruction.getData());
                            break;
                        case BAN_PLAYER:
                            Packet1ABanPlayers.handle((String) instruction.getData());
                            break;
                        case UNBAN_PLAYER:
                            Packet1BPardonPlayers.handle((String) instruction.getData());
                            break;
                        case CLEAR_INGAME_CACHE:
                            break;
                        case NOTIFICATIONS:
                            Packet1FNotifications.handle((NotificationData) instruction.getData());
                            break;
                        case PLUGIN_VERSION:
                            Packet14NewerVersion.handle((String) instruction.getData());
                            break;
                        default:
                            continue;
                    }
                }
            }
        }
    }

    private List<String> getWorlds() {
        return Bukkit.getWorlds().stream().map(World::getName).collect(Collectors.toList());
    }

    private List<String> getGroups() {
        return null;
    }

    private int getMaxPlayers() {
        return Bukkit.getMaxPlayers();
    }

    private int getOnlineCount() {
        return Bukkit.getOnlinePlayers().size();
    }

    private List<PlayerInfo> getOnlinePlayers() {
        return Bukkit.getOnlinePlayers().stream().map(player -> new PlayerInfo(player.getName(), player.getUniqueId())).collect(Collectors.toList());
    }
}
