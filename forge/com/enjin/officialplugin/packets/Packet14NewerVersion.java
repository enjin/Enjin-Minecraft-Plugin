package com.enjin.officialplugin.packets;

import java.io.BufferedInputStream;
import java.io.File;

import net.minecraft.server.MinecraftServer;

import com.enjin.officialplugin.EnjinMinecraftPlugin;
import com.enjin.officialplugin.threaded.DownloadPluginThread;

public class Packet14NewerVersion {
	
	public static void handle(BufferedInputStream in, EnjinMinecraftPlugin plugin) {
		try {
			String newversion = PacketUtilities.readString(in);
			plugin.debug("Got version string: " + newversion);
			if(plugin.autoupdate && !plugin.hasupdate) {
				if(plugin.updatefailed) {
					return;
				}
				//plugin.newversion = newversion;
				plugin.hasupdate = true;
				DownloadPluginThread downloader = new DownloadPluginThread("Mods", newversion, new File("Mods" + File.separator + "EnjinMinecraftPlugin.zip"), plugin);
				Thread downloaderthread = new Thread(downloader);
				downloaderthread.start();
				plugin.debug("Updating to new version " + newversion);
			}
		} catch (Throwable t) {
			MinecraftServer.logger.warning("Failed to dispatch command via 0x14, " + t.getMessage());
			t.printStackTrace();
		}
	}
}