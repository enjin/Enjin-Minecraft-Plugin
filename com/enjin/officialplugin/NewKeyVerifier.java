package com.enjin.officialplugin;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class NewKeyVerifier implements Runnable {
	
	EnjinMinecraftPlugin plugin;
	String key;
	CommandSender sender;
	Player player = null;
	public boolean completed = false;
	public boolean pluginboot = true;
	
	public NewKeyVerifier(EnjinMinecraftPlugin plugin, String key, CommandSender sender, boolean pluginboot) {
		this.plugin = plugin;
		this.key = key;
		this.sender = sender;
		if(sender instanceof Player) {
			player = (Player)sender;
		}
		this.pluginboot = pluginboot;
	}

	@Override
	public void run() {
		if(pluginboot) {
			int validation = keyValid(false, key);
			if(validation == 1) {
				plugin.debug("Key valid.");
				plugin.startTask();
				plugin.registerEvents();
			} else if(validation == 0){
				Bukkit.getLogger().warning("[Enjin Minecraft Plugin] Invalid key! Please regenerate your key and try again.");
			} else {
				Bukkit.getLogger().warning("[Enjin Minecraft Plugin] There was a problem connecting to Enjin, please try again in a few minutes. (If you continue to see this message, please type \"/enjin report\" and send the enjinreport_xxx.txt file to Enjin Support for further assistance.)");
			}
			completed = true;
		}else {
			if(key.equals(EnjinMinecraftPlugin.getHash())) {
				if(player == null || player.isOnline()) {
					sender.sendMessage(ChatColor.YELLOW + "The speficied key and the existing one are the same!");
				}
				completed = true;
				return;
			}
			int validation = keyValid(true, key);
			if(validation == 0) {
				if(player == null || player.isOnline()) {
					sender.sendMessage(ChatColor.RED + "That key is invalid! Make sure you've entered it properly!");
				}
				plugin.stopTask();
				plugin.unregisterEvents();
				completed = true;
				return;
			}else if(validation == 2) {
				if(player == null || player.isOnline()) {
					sender.sendMessage(ChatColor.RED + "There was a problem connecting to Enjin, please try again in a few minutes. (If you continue to see this message, please type \"/enjin report\" and send the enjinreport_xxx.txt file to Enjin Support for further assistance.)");
				}
				plugin.stopTask();
				plugin.unregisterEvents();
				completed = true;
				return;
			}
			EnjinMinecraftPlugin.setHash(key);
			plugin.debug("Writing hash to file.");
			plugin.config.set("authkey", key);
			plugin.saveConfig();
			if(player == null || player.isOnline()) {
				sender.sendMessage(ChatColor.GREEN + "Set the enjin key to " + key);
			}
			plugin.stopTask();
			plugin.unregisterEvents();
			plugin.startTask();
			plugin.registerEvents();
			completed = true;
		}
		completed = true;
	}

	private int keyValid(boolean save, String key) {
		try {
			if(key == null) {
				return 0;
			}
			if(key.length() < 2) {
				return 0;
			}
			if(save) {
				return EnjinMinecraftPlugin.sendAPIQuery("minecraft-auth", "key=" + key, "port=" + EnjinMinecraftPlugin.minecraftport, "save=1"); //save
			} else {
				return EnjinMinecraftPlugin.sendAPIQuery("minecraft-auth", "key=" + key, "port=" + EnjinMinecraftPlugin.minecraftport); //just check info
			}
		} catch (Throwable t) {
			Bukkit.getLogger().warning("[Enjin Minecraft Plugin] There was an error synchronizing game data to the enjin server.");
			t.printStackTrace();
			return 2;
		}
	}

}
