package com.enjin.officialplugin.points;

import com.enjin.officialplugin.ChatColor;

import net.minecraft.command.ICommandSender;
import net.minecraft.util.ChatMessageComponent;

public class RetrievePointsSyncClass implements Runnable {
	
	String playername;
	ICommandSender sender;
	boolean self;

	public RetrievePointsSyncClass(ICommandSender sender, String playername, boolean self) {
		this.playername = playername;
		this.sender = sender;
		this.self = self;
	}
	
	@Override
	public synchronized void run() {
		try {
			int amount = PointsAPI.getPointsForPlayer(playername);
			if(self) {
				sender.sendChatToPlayer(ChatMessageComponent.createFromText(ChatColor.GREEN + "You have " + ChatColor.GOLD + String.valueOf(amount) + " points."));
			}else {
				sender.sendChatToPlayer(ChatMessageComponent.createFromText(ChatColor.GREEN + playername + " has " + ChatColor.GOLD + String.valueOf(amount) + " points."));
			}
		} catch (PlayerDoesNotExistException e) {
			sender.sendChatToPlayer(ChatMessageComponent.createFromText(ChatColor.DARK_RED + "Enjin Error: That player has not registered on the website yet! In order to use this feature the player must be added on the website."));
		} catch (ErrorConnectingToEnjinException e) {
			sender.sendChatToPlayer(ChatMessageComponent.createFromText(ChatColor.DARK_RED + "Enjin Error: We're unable to connect to enjin at this current time, please try again later."));
		}
	}

}
