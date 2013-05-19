package com.enjin.officialplugin.threaded;

import net.canarymod.Canary;
import net.canarymod.api.entity.living.humanoid.Player;
import net.canarymod.tasks.ServerTask;

import com.enjin.officialplugin.EnjinMinecraftPlugin;

public class CommandExecuter extends ServerTask implements Runnable {

	Player sender;
	String command;
	
	public CommandExecuter(Player sender, String command, EnjinMinecraftPlugin plugin) {
		super(plugin, 0);
		this.sender = sender;
		this.command = command;
	}
	@Override
	public void run() {
		if(sender == null) {
			Canary.getServer().consoleCommand(command);
		}else {
			Canary.getServer().consoleCommand(command, sender);
		}
	}

}
