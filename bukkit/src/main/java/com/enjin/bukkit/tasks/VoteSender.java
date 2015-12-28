package com.enjin.bukkit.tasks;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.enjin.bukkit.config.EMPConfig;
import com.enjin.bukkit.EnjinMinecraftPlugin;
import com.enjin.common.utils.ConnectionUtil;
import com.enjin.core.Enjin;
import com.enjin.core.EnjinServices;
import com.enjin.rpc.mappings.mappings.general.RPCData;
import com.enjin.rpc.mappings.services.VoteService;

public class VoteSender implements Runnable {
    private EnjinMinecraftPlugin plugin;
    private boolean firstrun = true;

    public VoteSender(EnjinMinecraftPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void run() {
        EMPConfig config = Enjin.getConfiguration(EMPConfig.class);
        if (plugin.getPlayerVotes().size() > 0) {
            if (firstrun && config.isHttps()) {
                if (!ConnectionUtil.testHTTPSconnection()) {
                    config.setHttps(false);
                    plugin.getLogger().warning("SSL test connection failed, The plugin will use http without SSL. This may be less secure.");
                    Enjin.getLogger().warning("SSL test connection failed, The plugin will use http without SSL. This may be less secure.");
                }
            }

            Map<String, List<Object[]>> votes = new HashMap<>(plugin.getPlayerVotes());
            plugin.getPlayerVotes().clear();

            RPCData<String> data = EnjinServices.getService(VoteService.class).get(votes);
            String success;

            if (data == null) {
                Enjin.getLogger().debug("Voting data is null.");
                return;
            }

            if (data.getError() == null) {
                success = data.getResult();
            } else {
                Enjin.getLogger().warning(data.getError().getMessage());
                return;
            }

            if (success.equalsIgnoreCase("ok")) {
                Enjin.getLogger().info("Vote data successfully transferred.");
            } else {
                Enjin.getLogger().info("Vote data failed to transfer.");
            }
        }
    }
}
