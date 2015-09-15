package com.enjin.officialplugin.sync;

import com.enjin.core.EnjinServices;
import com.enjin.officialplugin.EnjinMinecraftPlugin;
import com.enjin.officialplugin.sync.processors.Execute;
import com.enjin.rpc.mappings.mappings.general.RPCData;
import com.enjin.rpc.mappings.mappings.plugin.Instruction;
import com.enjin.rpc.mappings.mappings.plugin.PlayerInfo;
import com.enjin.rpc.mappings.mappings.plugin.Status;
import com.enjin.rpc.mappings.mappings.plugin.SyncResponse;
import com.enjin.rpc.mappings.mappings.plugin.data.ExecuteData;
import com.enjin.rpc.mappings.services.PluginService;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class RPCPacketManager implements Runnable {
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
                null,
                null);

        PluginService service = EnjinServices.getService(PluginService.class);
        RPCData<SyncResponse> data = service.sync(plugin.getAuthKey(), status);

        if (data == null) {
            EnjinMinecraftPlugin.debug("Data is null while requesting sync update from Plugin.sync.");
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
                            break;
                        case REMOVE_PLAYER_GROUP:
                            break;
                        case EXECUTE:
                            Execute.handle((ExecuteData) instruction.getData());
                            break;
                        case EXECUTE_AS:
                            break;
                        case CONFIRMED_COMMANDS:
                            break;
                        case CONFIG:
                            break;
                        case ADD_PLAYER_WHITELIST:
                            break;
                        case REMOVE_PLAYER_WHITELIST:
                            break;
                        case RESPONSE_STATUS:
                            break;
                        case BAN_PLAYER:
                            break;
                        case UNBAN_PLAYER:
                            break;
                        case CLEAR_INGAME_CACHE:
                            break;
                        case NOTIFICATIONS:
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
