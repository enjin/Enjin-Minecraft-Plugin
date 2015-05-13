package com.enjin.officialplugin.packets;

import java.io.BufferedInputStream;
import java.io.File;

import org.bukkit.Bukkit;

import com.enjin.officialplugin.EnjinMinecraftPlugin;
import com.enjin.officialplugin.threaded.DownloadPluginThread;

public class Packet14DummyHandler {

    public static void handle(BufferedInputStream in, EnjinMinecraftPlugin plugin) {
        try {
            String newversion = PacketUtilities.readString(in);
        } catch (Throwable t) {
            Bukkit.getLogger().warning("Failed to dispatch command via 0x14, " + t.getMessage());
            t.printStackTrace();
        }
    }
}