package com.enjin.bukkit.threaded;

import java.util.concurrent.ConcurrentLinkedQueue;

import com.enjin.bukkit.CommandWrapper;
import com.enjin.bukkit.EnjinMinecraftPlugin;
import com.enjin.core.Enjin;
import org.bukkit.Bukkit;

public class CommandExecuter implements Runnable {

    ConcurrentLinkedQueue<CommandWrapper> commandqueue = new ConcurrentLinkedQueue<CommandWrapper>();
    EnjinMinecraftPlugin plugin;

    public CommandExecuter(EnjinMinecraftPlugin plugin) {
        this.plugin = plugin;
    }

    public void addCommand(CommandWrapper wrapper) {
        commandqueue.add(wrapper);
    }

    @Override
    public void run() {
        CommandWrapper comm;
        boolean dirty = false;
        while ((comm = commandqueue.poll()) != null) {
            dirty = true;
            Enjin.getPlugin().debug("Executing queued value: " + comm.getCommand());
            Bukkit.getServer().dispatchCommand(comm.getSender(), comm.getCommand());
            comm.setResult(plugin.getLastLogLine());
            Enjin.getPlugin().debug("Result: " + comm.getResult());
        }
        if (dirty) {
            plugin.saveCommandIDs();
        }
    }
}