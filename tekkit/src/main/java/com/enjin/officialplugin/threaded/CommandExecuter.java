package com.enjin.officialplugin.threaded;

import java.util.concurrent.ConcurrentLinkedQueue;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

import com.enjin.officialplugin.CommandWrapper;
import com.enjin.officialplugin.EnjinMinecraftPlugin;

public class CommandExecuter implements Runnable {

    ConcurrentLinkedQueue<CommandWrapper> commandqueue = new ConcurrentLinkedQueue<CommandWrapper>();

    public void addCommand(CommandSender sender, String command) {
        commandqueue.add(new CommandWrapper(sender, command));
    }

    @Override
    public void run() {
        CommandWrapper comm;
        while ((comm = commandqueue.poll()) != null) {
            EnjinMinecraftPlugin.debug("Executing queued command: " + comm.getCommand());
            Bukkit.getServer().dispatchCommand(comm.getSender(), comm.getCommand());
        }
    }
}