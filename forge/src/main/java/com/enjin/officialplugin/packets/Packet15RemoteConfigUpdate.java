package com.enjin.officialplugin.packets;

import java.io.BufferedInputStream;

import net.minecraft.server.MinecraftServer;

import com.enjin.officialplugin.ConfigValueTypes;
import com.enjin.officialplugin.EnjinMinecraftPlugin;

/**
 * @author OverCaste (Enjin LTE PTD).
 *         This software is released under an Open Source license.
 * @copyright Enjin 2012.
 */

public class Packet15RemoteConfigUpdate {

    String toReviewer = "The only time enjin will send these values is at" +
            "the request of the server owner in the Enjin control panel";

    public static void handle(BufferedInputStream in, EnjinMinecraftPlugin plugin) {
        try {
            String values = PacketUtilities.readString(in);
            plugin.debug("Changing these values in the config: \"" + values);
            String[] splitvalues = values.split(",");
            for (String value : splitvalues) {
                String[] split = value.split(":");
                if (plugin.configvalues.containsKey(split[0].toLowerCase())) {
                    ConfigValueTypes type = plugin.configvalues.get(split[0].toLowerCase());
                    switch (type) {
                        case BOOLEAN:
                            if (split[1].equals("0")) {
                                plugin.config.set(split[0].toLowerCase(), false);
                                plugin.config.save();
                            } else {
                                plugin.config.set(split[0].toLowerCase(), true);
                                plugin.config.save();
                            }
                            break;
                        case STRING:
                            plugin.config.set(split[0].toLowerCase(), split[1]);
                            plugin.config.save();
                            break;
                        case DOUBLE:
                            try {
                                double number = Double.parseDouble(split[1]);
                                plugin.config.set(split[0].toLowerCase(), number);
                                plugin.config.save();
                            } catch (NumberFormatException e) {
                                MinecraftServer.getServer().logWarning("[Enjin Minecraft Plugin] Unable to set " + split[0] + " to " + split[1] + " as it is not a double.");
                            }
                            break;
                        case FLOAT:
                            try {
                                float number = Float.parseFloat(split[1]);
                                plugin.config.set(split[0].toLowerCase(), number);
                                plugin.config.save();
                            } catch (NumberFormatException e) {
                                MinecraftServer.getServer().logWarning("[Enjin Minecraft Plugin] Unable to set " + split[0] + " to " + split[1] + " as it is not a float.");
                            }
                            break;
                        case INT:
                            try {
                                int number = Integer.parseInt(split[1]);
                                plugin.config.set(split[0].toLowerCase(), number);
                                plugin.config.save();
                            } catch (NumberFormatException e) {
                                MinecraftServer.getServer().logWarning("[Enjin Minecraft Plugin] Unable to set " + split[0] + " to " + split[1] + " as it is not an int.");
                            }
                            break;
                        case FORBIDDEN:
                            MinecraftServer.getServer().logWarning("[Enjin Minecraft Plugin] Enjin tried setting the value " + split[0] + " to " + split[1] + " but was forbidden!");
                    }
                } else {
                    MinecraftServer.getServer().logWarning("[Enjin Minecraft Plugin] Enjin tried setting the value " + split[0] + " to " + split[1] + " but it doesn't exist!");
                }
            }
            plugin.initFiles();
            //Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), PacketUtilities.readString(in));
        } catch (Throwable t) {
            MinecraftServer.getServer().logWarning("[Enjin Minecraft Plugin] Failed to set config variables via 0x15, " + t.getMessage());
            t.printStackTrace();
        }
    }
}
