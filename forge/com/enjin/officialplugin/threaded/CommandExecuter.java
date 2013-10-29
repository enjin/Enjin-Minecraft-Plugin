package com.enjin.officialplugin.threaded;

import java.util.concurrent.ConcurrentLinkedQueue;

import com.enjin.officialplugin.CommandWrapper;
import com.enjin.officialplugin.EnjinMinecraftPlugin;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;

public class CommandExecuter implements Runnable {
	
	EnjinMinecraftPlugin plugin;
	
	public CommandExecuter(EnjinMinecraftPlugin plugin) {
		this.plugin = plugin;
	}
	
	ConcurrentLinkedQueue<CommandWrapper> commandqueue = new ConcurrentLinkedQueue<CommandWrapper>();
	
	public synchronized void addCommand(String command) {
		plugin.debug("Adding command to queue: " + command);
		commandqueue.add(new CommandWrapper(command));
	}
	
	@Override
	public synchronized void run() {
		plugin.debug("Running queued commands...");
		CommandWrapper comm;
		
		while(!commandqueue.isEmpty()) {
			comm = commandqueue.poll();
			plugin.debug("Executing queued command: " + comm.getCommand());
			MinecraftServer.getServer().getCommandManager().executeCommand(MinecraftServer.getServer(), comm.getCommand());
		}
	}

}
