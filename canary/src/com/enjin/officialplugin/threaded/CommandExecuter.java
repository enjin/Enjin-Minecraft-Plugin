package com.enjin.officialplugin.threaded;

import java.util.concurrent.ConcurrentLinkedQueue;

import net.canarymod.Canary;
import net.canarymod.api.entity.living.humanoid.Player;
import net.canarymod.tasks.ServerTask;

import com.enjin.officialplugin.CommandWrapper;
import com.enjin.officialplugin.EnjinMinecraftPlugin;

public class CommandExecuter extends ServerTask implements Runnable {
	
	ConcurrentLinkedQueue<CommandWrapper> commandqueue = new ConcurrentLinkedQueue<CommandWrapper>();
	
	public void addCommand(Player sender, String command) {
		commandqueue.add(new CommandWrapper(sender, command));
	}
	
	public CommandExecuter(EnjinMinecraftPlugin plugin) {
		super(plugin, 0);
	}
	@Override
	public void run() {
		CommandWrapper comm;
		while((comm = commandqueue.poll()) != null) {
			EnjinMinecraftPlugin.debug("Executing queued command: " + comm.getCommand());
			if(comm.getSender() == null) {
				Canary.getServer().consoleCommand(comm.getCommand());
			}else {
				Canary.getServer().consoleCommand(comm.getCommand(), comm.getSender());
			}
		}
	}

}
