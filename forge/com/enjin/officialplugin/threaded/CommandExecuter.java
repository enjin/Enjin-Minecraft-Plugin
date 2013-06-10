package com.enjin.officialplugin.threaded;

import java.util.concurrent.ConcurrentLinkedQueue;

import com.enjin.officialplugin.CommandWrapper;
import com.enjin.officialplugin.EnjinMinecraftPlugin;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;

public class CommandExecuter implements Runnable {
	
	ConcurrentLinkedQueue<CommandWrapper> commandqueue = new ConcurrentLinkedQueue<CommandWrapper>();
	
	public void addCommand(String command) {
		commandqueue.add(new CommandWrapper(command));
	}
	@Override
	public void run() {
		CommandWrapper comm;
		while((comm = commandqueue.poll()) != null) {
			EnjinMinecraftPlugin.debug("Executing queued command: " + comm.getCommand());
			MinecraftServer.getServer().executeCommand(comm.getCommand());
		}
	}

}
