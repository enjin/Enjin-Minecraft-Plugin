package com.enjin.officialplugin.sync;

import com.enjin.core.Enjin;
import com.enjin.core.InstructionHandler;
import com.enjin.officialplugin.EnjinMinecraftPlugin;
import com.enjin.rpc.mappings.mappings.plugin.ExecutedCommand;
import org.bukkit.Bukkit;
import org.bukkit.World;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class BukkitInstructionHandler implements InstructionHandler {
    @Override
    public void addToWhitelist(String player) {
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "whitelist add " + player);
    }

    @Override
    public void removeFromWhitelist(String player) {
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "whitelist remove " + player);
    }

    @Override
    public void ban(String player) {
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "ban " + player);
    }

    @Override
    public void pardon(String player) {
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "pardon " + player);
    }

    @Override
    public void addToGroup(String player, String group, String world) {
        if (!Bukkit.getPluginManager().isPluginEnabled("Vault") || EnjinMinecraftPlugin.permission == null) {
            return;
        }

        if (world == null || world.isEmpty() || world.equals("*") || Bukkit.getWorld(world) == null) {
            EnjinMinecraftPlugin.permission.playerAddGroup((World) null, player, group);
        } else {
            EnjinMinecraftPlugin.permission.playerAddGroup(Bukkit.getWorld(world), player, group);
        }
    }

    @Override
    public void removeFromGroup(String player, String group, String world) {
        if (!Bukkit.getPluginManager().isPluginEnabled("Vault") || EnjinMinecraftPlugin.permission == null) {
            return;
        }

        if (world == null || world.isEmpty() || world.equals("*") || Bukkit.getWorld(world) == null) {
            EnjinMinecraftPlugin.permission.playerAddGroup((World) null, player, group);
        } else {
            EnjinMinecraftPlugin.permission.playerAddGroup(Bukkit.getWorld(world), player, group);
        }
    }

    @Override
    public void execute(long id, String command, long delay) {
        for (ExecutedCommand c : RPCPacketManager.getExecutedCommands()) {
            if (Long.parseLong(c.getId()) == id) {
                return;
            }
        }

        Bukkit.getScheduler().scheduleSyncDelayedTask(EnjinMinecraftPlugin.instance, () -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command), delay <= 0 ? 0 : delay * 20);

        RPCPacketManager.getExecutedCommands().add(new ExecutedCommand(Long.toString(id), command, ""));
    }

    @Override
    public void commandConfirmed(List<Long> executed) {
        for (long id : executed) {
            Enjin.getPlugin().debug("Confirming Command ID: " + id);
            new ArrayList<>(RPCPacketManager.getExecutedCommands()).stream().filter(command -> Long.parseLong(command.getId()) == id).forEach(command -> RPCPacketManager.getExecutedCommands().remove(command));
        }
    }

    @Override
    public void configUpdated(Map<String, Object> updates) {
        // TODO
    }

    @Override
    public void statusReceived(String status) {
        Enjin.getPlugin().debug("Enjin Status: " + status);
    }

    @Override
    public void clearInGameCache(String player, int id, String price) {
        // TODO
    }

    @Override
    public void notify(List<String> player, String message, long time) {
        player.stream().filter(p -> Bukkit.getPlayer(p) != null).forEach(p -> Bukkit.getPlayer(p).sendMessage(message));
    }

    @Override
    public void version(String version) {
        // TODO
    }
}
