package com.enjin.officialplugin;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class NewKeyVerifier implements Runnable {
	
	EnjinMinecraftPlugin plugin;
	String key;
	CommandSender sender;
	Player player;
	public boolean completed = false;
	public boolean pluginboot = true;
	
	public NewKeyVerifier(EnjinMinecraftPlugin plugin, String key, CommandSender sender, boolean pluginboot) {
		this.plugin = plugin;
		this.key = key;
		this.sender = sender;
		if(sender instanceof Player) {
			player = (Player)sender;
		}
	}

	@Override
	public void run() {
		if(pluginboot) {
			if(keyValid(false, key)) {
				plugin.debug("Key valid.");
				plugin.startTask();
				plugin.registerEvents();
			} else {
				Bukkit.getLogger().warning("[Enjin Minecraft Plugin] Failed to authenticate with enjin! This may mean your key is invalid, or there was an error connecting.");
			}
		}else {
			if(key.equals(EnjinMinecraftPlugin.getHash())) {
				if(player == null || player.isOnline()) {
					sender.sendMessage(ChatColor.YELLOW + "The speficied key and the existing one are the same!");
				}
				completed = true;
				return;
			}
			if(!keyValid(true, key)) {
				if(player == null || player.isOnline()) {
					sender.sendMessage(ChatColor.RED + "That key is invalid! Make sure you've entered it properly!");
				}
				plugin.stopTask();
				plugin.unregisterEvents();
				completed = true;
				return;
			}
			EnjinMinecraftPlugin.setHash(key);
			try {
				plugin.debug("Writing hash to file.");
				BufferedWriter writer = new BufferedWriter(new FileWriter(plugin.hashFile));
				writer.write(key);
				writer.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			if(player == null || player.isOnline()) {
				sender.sendMessage(ChatColor.GREEN + "Set the enjin key to " + key);
			}
			plugin.stopTask();
			plugin.unregisterEvents();
			plugin.startTask();
			plugin.registerEvents();
			completed = true;
		}
	}

	private boolean keyValid(boolean save, String key) {
		try {
			if(key == null) {
				return false;
			}
			if(key.length() < 2) {
				return false;
			}
			if(save) {
				return EnjinMinecraftPlugin.sendAPIQuery("minecraft-auth", "key=" + key, "port=" + EnjinMinecraftPlugin.minecraftport, "save=1"); //save
			} else {
				return EnjinMinecraftPlugin.sendAPIQuery("minecraft-auth", "key=" + key, "port=" + EnjinMinecraftPlugin.minecraftport); //just check info
			}
		} catch (Throwable t) {
			Bukkit.getLogger().warning("[Enjin Minecraft Plugin] There was an error synchronizing game data to the enjin server.");
			t.printStackTrace();
			return false;
		}
	}

}
