package com.enjin.officialplugin.sync.data;

import java.io.BufferedInputStream;

import com.enjin.officialplugin.util.PacketUtilities;
import org.bukkit.Bukkit;

import com.enjin.officialplugin.EnjinMinecraftPlugin;

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