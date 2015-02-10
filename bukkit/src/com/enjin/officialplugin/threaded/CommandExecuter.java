package com.enjin.officialplugin.threaded;

import java.util.concurrent.ConcurrentLinkedQueue;

import org.bukkit.Bukkit;
import com.enjin.officialplugin.CommandWrapper;
import com.enjin.officialplugin.EnjinMinecraftPlugin;

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
		while((comm = commandqueue.poll()) != null) {
			dirty = true;
			EnjinMinecraftPlugin.debug("Executing queued command: " + comm.getCommand());
			Bukkit.getServer().dispatchCommand(comm.getSender(), comm.getCommand());
			comm.setResult(plugin.getLastLogLine());
			EnjinMinecraftPlugin.debug("Result: " + comm.getResult());
		}
		if(dirty) {
			plugin.saveCommandIDs();
		}
	}
}