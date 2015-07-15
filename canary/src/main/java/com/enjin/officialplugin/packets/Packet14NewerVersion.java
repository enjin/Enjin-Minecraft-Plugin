package com.enjin.officialplugin.packets;

import java.io.BufferedInputStream;

import com.enjin.officialplugin.EnjinMinecraftPlugin;

public class Packet14NewerVersion {

    public static void handle(BufferedInputStream in, EnjinMinecraftPlugin plugin) {
        try {
            String newversion = PacketUtilities.readString(in);
            EnjinMinecraftPlugin.debug("Got version string: " + newversion);
            if (plugin.autoupdate && !plugin.hasupdate) {
                if (plugin.updatefailed) {
                    return;
                }
                //This isn't an official port, so let's disable updating.
                /*
                //plugin.newversion = newversion;
				plugin.hasupdate = true;
				DownloadPluginThread downloader = new DownloadPluginThread(plugin.getDataFolder().getParent(), newversion, new File(plugin.getDataFolder().getParent() + File.separator + "EnjinMinecraftPlugin.jar"), plugin);
				Thread downloaderthread = new Thread(downloader);
				downloaderthread.start();
				plugin.debug("Updating to new version " + newversion);*/
            }
        } catch (Throwable t) {
            EnjinMinecraftPlugin.logger.warning("Failed to dispatch command via 0x14, " + t.getMessage());
            t.printStackTrace();
        }
    }
}