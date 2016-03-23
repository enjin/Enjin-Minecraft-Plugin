package com.enjin.sponge.sync;

import com.enjin.core.Enjin;
import com.enjin.core.EnjinServices;
import com.enjin.rpc.mappings.mappings.plugin.PlayerGroupInfo;
import com.enjin.sponge.EnjinMinecraftPlugin;
import com.enjin.rpc.mappings.mappings.general.RPCData;
import com.enjin.rpc.mappings.mappings.plugin.PlayerInfo;
import com.enjin.rpc.mappings.mappings.plugin.Status;
import com.enjin.rpc.mappings.mappings.plugin.SyncResponse;
import com.enjin.rpc.mappings.services.PluginService;
import com.enjin.sponge.config.RankUpdatesConfig;
import com.enjin.sponge.listeners.ConnectionListener;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.world.World;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
                plugin.getContainer().getVersion().get(),
                getWorlds(),
                getGroups(),
                getMaxPlayers(),
                getOnlineCount(),
                getOnlinePlayers(),
                getPlayerGroups(),
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
        return ConnectionListener.getGroups();
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
}
