package com.enjin.sponge.tasks;

import com.enjin.core.Enjin;
import com.enjin.core.EnjinServices;
import com.enjin.rpc.mappings.mappings.general.RPCData;
import com.enjin.rpc.mappings.services.VoteService;
import com.enjin.sponge.EnjinMinecraftPlugin;
import com.enjin.sponge.managers.VotifierManager;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class VoteSender implements Runnable {
    private EnjinMinecraftPlugin plugin;

    public VoteSender (EnjinMinecraftPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void run() {
        if (VotifierManager.getPlayerVotes().size() > 0) {
            Map<String, List<Object[]>> votes = new HashMap<>(VotifierManager.getPlayerVotes());
            VotifierManager.getPlayerVotes().clear();

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
