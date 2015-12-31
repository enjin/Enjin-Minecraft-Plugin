package com.enjin.bungee.sync;

import com.enjin.bungee.EnjinMinecraftPlugin;
import com.enjin.core.Enjin;
import com.enjin.core.InstructionHandler;
import com.enjin.core.config.EnjinConfig;
import com.google.common.base.Optional;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;

import java.io.File;
import java.util.List;

public class BungeeInstructionHandler implements InstructionHandler {
    @Override
    public void addToWhitelist(String player) {
    }

    @Override
    public void removeFromWhitelist(String player) {
    }

    @Override
    public void ban(String player) {
    }

    @Override
    public void pardon(String player) {
    }

    @Override
    public void addToGroup(String player, String group, String world) {
    }

    @Override
    public void removeFromGroup(String player, String group, String world) {
    }

    @Override
    public void execute(long id, String command, long delay, Optional<Boolean> requireOnline, Optional<String> name, Optional<String> uuid) {
    }

    @Override
    public void commandConfirmed(List<Long> executed) {
    }

    @Override
    public void configUpdated(Object update) {
        EnjinConfig config = Enjin.getConfiguration();
        if (config != null) {
            config.update(new File(EnjinMinecraftPlugin.getInstance().getDataFolder(), "config.json"), update);
            EnjinMinecraftPlugin.getInstance().initConfig();
            EnjinMinecraftPlugin.saveConfiguration();
        }
    }

    @Override
    public void statusReceived(String status) {
        Enjin.getPlugin().debug("Enjin Status: " + status);
    }

    @Override
    public void clearInGameCache(String player, int id, String price) {
    }

    @Override
    public void notify(List<String> players, String message, long time) {
        for (String player : players) {
            ProxiedPlayer p = ProxyServer.getInstance().getPlayer(player);
            if (p != null) {
                p.sendMessage(message);
            }
        }
    }

    @Override
    public void version(String version) {
        // TODO
    }
}
