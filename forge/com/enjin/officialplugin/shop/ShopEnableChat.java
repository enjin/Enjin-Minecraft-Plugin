package com.enjin.officialplugin.shop;

import com.enjin.officialplugin.ChatColor;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;

public class ShopEnableChat extends CommandBase {
	
	ShopListener sl;

	public ShopEnableChat(ShopListener listen) {
		sl = listen;
	}

	@Override
	public String getCommandName() {
		return "ec";
	}

	@Override
	public void processCommand(ICommandSender icommandsender, String[] astring) {
		/*
		EntityPlayerMP player = null;
		if(icommandsender instanceof EntityPlayerMP) {
			player = (EntityPlayerMP) icommandsender;
		}else {
			return;
		}
		if(sl.playersdisabledchat.containsKey(player.username.toLowerCase())) {
			sl.playersdisabledchat.remove(player.username.toLowerCase());
			player.sendChatToPlayer(ChatColor.GREEN + "Your chat is now enabled.");
		}*/
	}

}
