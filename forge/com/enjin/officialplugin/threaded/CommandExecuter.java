package com.enjin.officialplugin.threaded;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;

public class CommandExecuter implements Runnable {

	EntityPlayerMP sender;
	String command;
	
	public CommandExecuter(EntityPlayerMP sender, String command) {
		this.sender = sender;
		this.command = command;
	}
	@Override
	public void run() {
		if(sender == null) {
			MinecraftServer.getServer().executeCommand(command);
		}else {
			//TODO: Execute command as a certain player.
		}
	}

}
