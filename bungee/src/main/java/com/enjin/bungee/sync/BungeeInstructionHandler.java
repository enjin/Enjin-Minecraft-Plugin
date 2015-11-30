package com.enjin.bungee.sync;

import com.enjin.bungee.EnjinMinecraftPlugin;
import com.enjin.bungee.config.EnjinConfig;
import com.enjin.core.Enjin;
import com.enjin.core.InstructionHandler;
import net.md_5.bungee.api.ProxyServer;

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
    public void execute(long id, String command, long delay) {
    }

    @Override
    public void commandConfirmed(List<Long> executed) {
    }

    @Override
    public void configUpdated(Object update) {
        EnjinConfig config = EnjinMinecraftPlugin.getConfiguration();
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
        players.stream().filter(p -> ProxyServer.getInstance().getPlayer(p) != null).forEach(p -> ProxyServer.getInstance().getPlayer(p).sendMessage(message));
    }

    @Override
    public void version(String version) {
        // TODO
    }
}
