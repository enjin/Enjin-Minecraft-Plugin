package com.enjin.officialplugin.packets;

import java.io.BufferedInputStream;
import java.io.File;

import net.md_5.bungee.BungeeCord;

import com.enjin.emp.bungee.DownloadPluginThread;
import com.enjin.emp.bungee.EnjinPlugin;

public class Packet14NewerVersion {

    public static void handle(BufferedInputStream in, EnjinPlugin plugin) {
        try {
            String newversion = PacketUtilities.readString(in);
            if (plugin.autoupdate && !plugin.hasupdate) {
                if (plugin.updatefailed) {
                    return;
                }
                //plugin.newversion = newversion;
                plugin.hasupdate = true;
                DownloadPluginThread downloader = new DownloadPluginThread(plugin.getDataFolder().getParent(), newversion, new File(plugin.getDataFolder().getParent() + File.separator + "EnjinMinecraftPlugin.jar"), plugin);
                BungeeCord.getInstance().getScheduler().runAsync(plugin, downloader);
                EnjinPlugin.debug("Updating to new version " + newversion);
            }
        } catch (Throwable t) {
            plugin.getLogger().warning("Failed to dispatch command via 0x14, " + t.getMessage());
            t.printStackTrace();
        }
    }
}