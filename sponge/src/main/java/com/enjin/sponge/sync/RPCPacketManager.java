package com.enjin.sponge.sync;

import com.enjin.core.EnjinServices;
import com.enjin.sponge.EnjinMinecraftPlugin;
import com.enjin.rpc.mappings.mappings.general.RPCData;
import com.enjin.rpc.mappings.mappings.plugin.PlayerInfo;
import com.enjin.rpc.mappings.mappings.plugin.Status;
import com.enjin.rpc.mappings.mappings.plugin.SyncResponse;
import com.enjin.rpc.mappings.services.PluginService;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.world.World;

import java.util.List;

public class RPCPacketManager implements Runnable {
    private EnjinMinecraftPlugin plugin;

    public RPCPacketManager(EnjinMinecraftPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void run() {
        Status status = new Status(System.getProperty("java.version"),
                null,
                getPlugins(),
                false,
                plugin.getContainer().getVersion(),
                getWorlds(),
                getGroups(),
                getMaxPlayers(),
                getOnlineCount(),
                getOnlinePlayers(),
                null,
                null,
                null,
                null);

        PluginService service = EnjinServices.getService(PluginService.class);
        RPCData<SyncResponse> data = service.sync(status);

        if (data == null) {
            return;
        }

        if (data.getError() != null) {
            plugin.getLogger().warn(data.getError().getMessage());
        } else {
            SyncResponse result = data.getResult();
            if (result != null && result.getStatus().equalsIgnoreCase("ok")) {
                // TODO: Process result
            }
        }
    }

    private List<String> getPlugins() {
        return Sponge.getPluginManager().getPlugins().stream().map(PluginContainer::getName).collect(Collectors.toList());
    }

    private List<String> getWorlds() {
        return plugin.getGame().getServer().getWorlds().stream().map(World::getName).collect(Collectors.toList());
    }

    private List<String> getGroups() {
        return null;
    }

    private int getMaxPlayers() {
        return plugin.getGame().getServer().getMaxPlayers();
    }

    private int getOnlineCount() {
        return plugin.getGame().getServer().getOnlinePlayers().size();
    }

    private List<PlayerInfo> getOnlinePlayers() {
        return plugin.getGame().getServer().getOnlinePlayers().stream().map(player -> new PlayerInfo(player.getName(), player.getUniqueId())).collect(Collectors.toList());
    }
}
