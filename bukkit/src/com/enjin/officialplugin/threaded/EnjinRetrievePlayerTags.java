package com.enjin.officialplugin.threaded;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.enjin.officialplugin.EnjinMinecraftPlugin;
import com.enjin.officialplugin.api.EnjinAPI;
import com.enjin.officialplugin.api.PlayerTag;
import com.enjin.officialplugin.points.ErrorConnectingToEnjinException;
import com.enjin.officialplugin.points.PlayerDoesNotExistException;

public class EnjinRetrievePlayerTags implements Runnable {
	
	String player;
	CommandSender requester;
	EnjinMinecraftPlugin plugin;

	public EnjinRetrievePlayerTags(String player, CommandSender requester, EnjinMinecraftPlugin plugin) {
		this.player = player;
		this.requester = requester;
		this.plugin = plugin;
	}
	
	@Override
	public void run() {
		try {
			DateFormat df = new SimpleDateFormat("yyyy/MM/dd HH:mm");
			ConcurrentHashMap<String, PlayerTag> ptags = EnjinAPI.getPlayerTags(player);
			if(ptags.size() > 0) {
				requester.sendMessage(ChatColor.GOLD + "Found tags for player " + player + "! Tags found:");
				Enumeration<PlayerTag> tags = ptags.elements(); 
				while(tags.hasMoreElements()) {
					PlayerTag element = tags.nextElement();
					requester.sendMessage(ChatColor.AQUA.toString() + ChatColor.BOLD + element.getTagName());
					requester.sendMessage(ChatColor.GOLD + "Tag ID: " + element.getTagID());
					if(element.getExpiryTime() > 0) {
						Date date = new Date(element.getExpiryTime());
						requester.sendMessage(ChatColor.GOLD + "Expires: " + df.format(date));
					}
					if(element.getCustomData("date_added") != null) {
						String date_added = element.getCustomData("date_added").toString();
						try {
							long date = Long.parseLong(date_added)*1000;
							if(date > 0) {
								Date added = new Date(date);
								requester.sendMessage(ChatColor.GOLD + "Added: " + df.format(added));
							}
						}catch (NumberFormatException e) {
							
						}
					}
				}
			}else {
				requester.sendMessage(ChatColor.GOLD + "No tags found for player " + player + "!");
			}
		} catch (PlayerDoesNotExistException e) {
			requester.sendMessage(ChatColor.RED + "I'm sorry, that player doesn't exist on the website!");
		} catch (ErrorConnectingToEnjinException e) {
			requester.sendMessage(ChatColor.RED + "Whoops! I can't connect to Enjin at the moment! Please try later.");
			requester.sendMessage(ChatColor.RED + e.getMessage());
		}
	}

}
