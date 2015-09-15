package com.enjin.officialplugin.threaded;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.enjin.core.EnjinServices;
import com.enjin.rpc.mappings.mappings.general.RPCData;
import com.enjin.rpc.mappings.services.VoteService;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import com.enjin.officialplugin.EnjinErrorReport;
import com.enjin.officialplugin.EnjinMinecraftPlugin;

public class PeriodicVoteTask implements Runnable {
    private EnjinMinecraftPlugin plugin;
    private boolean firstrun = true;

    public PeriodicVoteTask(EnjinMinecraftPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void run() {
        if (plugin.playervotes.size() > 0) {
            if (firstrun && EnjinMinecraftPlugin.usingSSL) {
                if (!plugin.testHTTPSconnection()) {
                    EnjinMinecraftPlugin.usingSSL = false;
                    plugin.getLogger().warning("SSL test connection failed, The plugin will use http without SSL. This may be less secure.");
                    EnjinMinecraftPlugin.enjinlogger.warning("SSL test connection failed, The plugin will use http without SSL. This may be less secure.");
                }
            }

            Map<String, List<String>> votes = new HashMap<String, List<String>>(plugin.playervotes);
            plugin.playervotes.clear();

            boolean successful;
            RPCData<String> data = EnjinServices.getService(VoteService.class).get(EnjinMinecraftPlugin.getAuthKey(), votes);
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
                if (plugin.unabletocontactenjin) {
                    plugin.unabletocontactenjin = false;
                    Player[] players = plugin.getPlayerGetter().getOnlinePlayers();
                    for (Player player : players) {
                        if (player.hasPermission("enjin.notify.connectionstatus")) {
                            player.sendMessage(ChatColor.DARK_GREEN + "[Enjin Minecraft Plugin] Connection to Enjin re-established!");
                            plugin.getLogger().info("Connection to Enjin re-established!");
                        }
                    }
                }
            } else if (success.equalsIgnoreCase("auth_error")) {
                plugin.authkeyinvalid = true;
                EnjinMinecraftPlugin.enjinlogger.warning("[Enjin Minecraft Plugin] Auth key invalid. Please regenerate on the enjin control panel.");
                plugin.getLogger().warning("Auth key invalid. Please regenerate on the enjin control panel.");
                plugin.stopTask();
                Player[] players = plugin.getPlayerGetter().getOnlinePlayers();
                for (Player player : players) {
                    if (player.hasPermission("enjin.notify.invalidauthkey")) {
                        player.sendMessage(ChatColor.DARK_RED + "[Enjin Minecraft Plugin] Auth key is invalid. Please generate a new one.");
                    }
                }
                successful = false;
            } else if (success.equalsIgnoreCase("bad_data")) {
                EnjinMinecraftPlugin.enjinlogger.warning("[Enjin Minecraft Plugin] Oops, we sent bad data, please send the enjin.log file to enjin to debug.");
                plugin.lasterror = new EnjinErrorReport("Enjin reported bad data", "Vote synch.");
                successful = false;
            } else if (success.equalsIgnoreCase("retry_later")) {
                EnjinMinecraftPlugin.enjinlogger.info("[Enjin Minecraft Plugin] Enjin said to wait, saving data for next sync.");
                successful = false;
            } else if (success.equalsIgnoreCase("connect_error")) {
                EnjinMinecraftPlugin.enjinlogger.info("[Enjin Minecraft Plugin] Enjin is having something going on, if you continue to see this error please report it to enjin.");
                plugin.lasterror = new EnjinErrorReport("Enjin reported a connection issue.", "Vote synch.");
                successful = false;
            } else if (success.startsWith("invalid_op")) {
                plugin.lasterror = new EnjinErrorReport(success, "Vote synch.");
                successful = false;
            } else {
                EnjinMinecraftPlugin.enjinlogger.info("[Enjin Minecraft Plugin] Something happened on vote sync, if you continue to see this error please report it to enjin.");
                EnjinMinecraftPlugin.enjinlogger.info("Response code: " + success);
                plugin.getLogger().info("Something happened on sync, if you continue to see this error please report it to enjin.");
                plugin.getLogger().info("Response code: " + success);
                successful = false;
            }

            if (!successful) {
                EnjinMinecraftPlugin.debug("Vote sync unsuccessful.");
                plugin.playervotes.putAll(votes);
            } else {
                EnjinMinecraftPlugin.debug("Vote sync successful.");
                firstrun = false;
            }
        }
    }
}
