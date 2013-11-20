package com.enjin.officialplugin.packets;

import java.io.BufferedInputStream;
import java.io.File;

import org.bukkit.Bukkit;

import com.enjin.officialplugin.EnjinMinecraftPlugin;
import com.enjin.officialplugin.threaded.DownloadPluginThread;

public class Packet14NewerVersion {
	
	public static void handle(BufferedInputStream in, EnjinMinecraftPlugin plugin) {
		try {
			String newversion = PacketUtilities.readString(in);
			if(plugin.autoupdate && !plugin.hasupdate) {
				if(plugin.updatefailed) {
					return;
				}
				//plugin.newversion = newversion;
				plugin.hasupdate = true;
				DownloadPluginThread downloader = new DownloadPluginThread(plugin.getDataFolder().getParent(), newversion, new File(plugin.getDataFolder().getParent() + File.separator + "EnjinMinecraftPlugin.jar"), plugin);
				Thread downloaderthread = new Thread(downloader);
				downloaderthread.start();
				EnjinMinecraftPlugin.debug("Updating to new version " + newversion);
			}
		} catch (Throwable t) {
			Bukkit.getLogger().warning("Failed to dispatch command via 0x14, " + t.getMessage());
			t.printStackTrace();
		}
	}
}