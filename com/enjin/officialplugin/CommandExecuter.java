package com.enjin.officialplugin;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

public class CommandExecuter implements Runnable {

	CommandSender sender;
	String command;
	
	public CommandExecuter(CommandSender sender, String command) {
		this.sender = sender;
		this.command = command;
	}
	@Override
	public void run() {
		Bukkit.getServer().dispatchCommand(sender, command);
	}

}
