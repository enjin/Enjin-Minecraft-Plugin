package com.enjin.bukkit.tasks;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.enjin.bukkit.config.EMPConfig;
import com.enjin.bukkit.util.Log;
import com.enjin.bukkit.util.io.EnjinErrorReport;
import com.enjin.bukkit.EnjinMinecraftPlugin;
import com.enjin.common.utils.ConnectionUtil;
import com.enjin.core.Enjin;
import com.enjin.core.EnjinServices;
import com.enjin.rpc.mappings.mappings.general.RPCData;
import com.enjin.rpc.mappings.services.VoteService;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

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
                    Log.warning("SSL test connection failed, The plugin will use http without SSL. This may be less secure.");
                }
            }

            Map<String, List<Object[]>> votes = new HashMap<>(plugin.getPlayerVotes());
            plugin.getPlayerVotes().clear();

            boolean successful;
            RPCData<String> data = EnjinServices.getService(VoteService.class).get(votes);
            String success;

            if (data == null) {
                plugin.debug("Voting data is null.");
                return;
            }

            if (data.getError() == null) {
                success = data.getResult();
            } else {
                plugin.getLogger().warning(data.getError().getMessage());
                return;
            }

            if (success.equalsIgnoreCase("ok")) {
                successful = true;
                if (plugin.isUnableToContactEnjin()) {
                    plugin.setUnableToContactEnjin(false);
                    Player[] players = Bukkit.getOnlinePlayers().toArray(new Player[]{});
                    for (Player player : players) {
                        if (player.hasPermission("enjin.notify.connectionstatus")) {
                            player.sendMessage(ChatColor.DARK_GREEN + "[Enjin Minecraft Plugin] Connection to Enjin re-established!");
                            plugin.getLogger().info("Connection to Enjin re-established!");
                        }
                    }
                }
            } else if (success.equalsIgnoreCase("auth_error")) {
                plugin.setAuthKeyInvalid(true);
                plugin.disableTasks();
                Log.warning("[Enjin Minecraft Plugin] Auth key invalid. Please regenerate on the enjin control panel.");
                plugin.getLogger().warning("Auth key invalid. Please regenerate on the enjin control panel.");
                Player[] players = Bukkit.getOnlinePlayers().toArray(new Player[]{});
                for (Player player : players) {
                    if (player.hasPermission("enjin.notify.invalidauthkey")) {
                        player.sendMessage(ChatColor.DARK_RED + "[Enjin Minecraft Plugin] Auth key is invalid. Please generate a new one.");
                    }
                }
                successful = false;
            } else if (success.equalsIgnoreCase("bad_data")) {
                Log.warning("[Enjin Minecraft Plugin] Oops, we sent bad data, please send the enjin.log file to enjin to debug.");
                plugin.setLastError(new EnjinErrorReport("Enjin reported bad data", "Vote synch."));
                successful = false;
            } else if (success.equalsIgnoreCase("retry_later")) {
                Log.info("[Enjin Minecraft Plugin] Enjin said to wait, saving data for next sync.");
                successful = false;
            } else if (success.equalsIgnoreCase("connect_error")) {
                Log.info("[Enjin Minecraft Plugin] Enjin is having something going on, if you continue to see this error please report it to enjin.");
                plugin.setLastError(new EnjinErrorReport("Enjin reported a connection issue.", "Vote synch."));
                successful = false;
            } else if (success.startsWith("invalid_op")) {
                plugin.setLastError(new EnjinErrorReport(success, "Vote synch."));
                successful = false;
            } else {
                Log.info("[Enjin Minecraft Plugin] Something happened on vote sync, if you continue to see this error please report it to enjin.");
                Log.info("Response code: " + success);
                plugin.getLogger().info("Something happened on sync, if you continue to see this error please report it to enjin.");
                plugin.getLogger().info("Response code: " + success);
                successful = false;
            }

            if (!successful) {
                Enjin.getPlugin().debug("Vote sync unsuccessful.");
                plugin.getPlayerVotes().putAll(votes);
            } else {
                Enjin.getPlugin().debug("Vote sync successful.");
                firstrun = false;
            }
        }
    }
}
