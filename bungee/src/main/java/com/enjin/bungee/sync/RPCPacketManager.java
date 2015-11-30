package com.enjin.bungee.sync;

import com.enjin.bungee.EnjinMinecraftPlugin;
import com.enjin.bungee.sync.data.NewerVersionInstruction;
import com.enjin.bungee.sync.data.NotificationsInstruction;
import com.enjin.bungee.sync.data.RemoteConfigUpdateInstruction;
import com.enjin.core.Enjin;
import com.enjin.core.EnjinServices;
import com.enjin.rpc.mappings.mappings.bungeecord.NodeState;
import com.enjin.rpc.mappings.mappings.general.RPCData;
import com.enjin.rpc.mappings.mappings.plugin.Instruction;
import com.enjin.rpc.mappings.mappings.plugin.PlayerInfo;
import com.enjin.rpc.mappings.mappings.plugin.Status;
import com.enjin.rpc.mappings.mappings.plugin.SyncResponse;
import com.enjin.rpc.mappings.mappings.plugin.data.NotificationData;
import com.enjin.rpc.mappings.services.BungeeCordService;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.config.ServerInfo;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class RPCPacketManager implements Runnable {
    private EnjinMinecraftPlugin plugin;

    public RPCPacketManager(EnjinMinecraftPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void run() {
        Status status = new Status(null,
                plugin.getDescription().getVersion(),
                null,
                null,
                getMaxPlayers(),
                getOnlineCount(),
                getOnlinePlayers(),
                null,
                null,
                null);

        Map<String, NodeState> servers = getServers();
        ProxyServer.getInstance().getScheduler().schedule(plugin, () -> {
            BungeeCordService service = EnjinServices.getService(BungeeCordService.class);
            RPCData<SyncResponse> data = service.get(EnjinMinecraftPlugin.getConfiguration().getAuthKey(), status, servers);

            if (data == null) {
                Enjin.getPlugin().debug("Data is null while requesting sync update from Bungeecord.get.");
                return;
            }

            if (data.getError() != null) {
                plugin.getLogger().warning(data.getError().getMessage());
            } else {
                SyncResponse result = data.getResult();
                if (result != null && result.getStatus().equalsIgnoreCase("ok")) {
                    for (Instruction instruction : result.getInstructions()) {
                        switch (instruction.getCode()) {
                            case CONFIG:
                                RemoteConfigUpdateInstruction.handle((Map<String, Object>) instruction.getData());
                                break;
                            case RESPONSE_STATUS:
                                Enjin.getPlugin().getInstructionHandler().statusReceived((String) instruction.getData());
                                break;
                            case NOTIFICATIONS:
                                NotificationsInstruction.handle((NotificationData) instruction.getData());
                                break;
                            case PLUGIN_VERSION:
                                NewerVersionInstruction.handle((String) instruction.getData());
                                break;
                            default:
                                continue;
                        }
                    }
                }
            }
        }, 5, TimeUnit.SECONDS);
    }

    private Integer getMaxPlayers() {
        return ProxyServer.getInstance().getConfig().getPlayerLimit();
    }

    private Integer getOnlineCount() {
        return ProxyServer.getInstance().getPlayers().size();
    }

    private List<PlayerInfo> getOnlinePlayers() {
        return ProxyServer.getInstance().getPlayers().stream().map(player -> new PlayerInfo(player.getName(), player.getUniqueId())).collect(Collectors.toList());
    }

    private Map<String, NodeState> getServers() {
        Map<String, NodeState> servers = new ConcurrentHashMap<>();

        for (Map.Entry<String, ServerInfo> server : ProxyServer.getInstance().getServers().entrySet()) {
            if (servers == null || server == null) {
                continue;
            }

            ServerInfo info = server.getValue();
            info.ping((ping, throwable) -> {
                if (throwable != null) {
                    servers.put(server.getKey(), new NodeState(null, null));
                    return;
                }

                servers.put(server.getKey() ,new NodeState(info.getPlayers().stream().map(ProxiedPlayer::getName).collect(Collectors.toList()), ping.getPlayers().getMax()));
            });
        }

        return servers;
    }
}
