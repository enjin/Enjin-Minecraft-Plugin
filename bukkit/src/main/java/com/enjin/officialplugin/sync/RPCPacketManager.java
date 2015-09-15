package com.enjin.officialplugin.sync;

import com.enjin.core.EnjinServices;
import com.enjin.officialplugin.EnjinMinecraftPlugin;
import com.enjin.rpc.mappings.mappings.general.RPCData;
import com.enjin.rpc.mappings.mappings.plugin.PlayerInfo;
import com.enjin.rpc.mappings.mappings.plugin.Status;
import com.enjin.rpc.mappings.mappings.plugin.SyncResponse;
import com.enjin.rpc.mappings.services.PluginService;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class RPCPacketManager implements Runnable {
    private EnjinMinecraftPlugin plugin;

    private RPCPacketManager(EnjinMinecraftPlugin plugin) {
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
            return;
        }

        if (data.getError() != null) {
            plugin.getLogger().warning(data.getError().getMessage());
        } else {
            SyncResponse result = data.getResult();
            if (result != null && result.getStatus().equalsIgnoreCase("ok")) {
                // TODO: Processe result
            }
        }
    }

    private List<String> getWorlds() {
        List<String> worlds = new ArrayList<String>();

        for (World world : Bukkit.getWorlds()) {
            worlds.add(world.getName());
        }

        return worlds;
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
        List<PlayerInfo> infos = new ArrayList<PlayerInfo>();

        for (Player player : Bukkit.getOnlinePlayers()) {
            infos.add(new PlayerInfo(player.getName(), player.getUniqueId()));
        }

        return infos;
    }
}
