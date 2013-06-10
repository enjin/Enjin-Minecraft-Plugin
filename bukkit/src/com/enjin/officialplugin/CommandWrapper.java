package com.enjin.officialplugin;

import org.bukkit.command.CommandSender;

public class CommandWrapper {
	
	String command;
	CommandSender sender;
	
	public CommandWrapper(CommandSender sender, String command) {
		this.sender = sender;
		this.command = command;
	}

	public String getCommand() {
		return command;
	}

	public CommandSender getSender() {
		return sender;
	}

}
