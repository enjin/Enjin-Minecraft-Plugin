package com.enjin.officialplugin.packets;

import java.io.BufferedInputStream;

import org.bukkit.Bukkit;

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
            EnjinMinecraftPlugin.debug("Changing these values in the config: \"" + values);
            String[] splitvalues = values.split(",");
            for (String value : splitvalues) {
                String[] split = value.split(":");
                if (plugin.configvalues.containsKey(split[0].toLowerCase())) {
                    ConfigValueTypes type = plugin.configvalues.get(split[0].toLowerCase());
                    switch (type) {
                        case BOOLEAN:
                            if (split[1].equals("0")) {
                                plugin.config.set(split[0].toLowerCase(), false);
                                plugin.saveConfig();
                            } else {
                                plugin.config.set(split[0].toLowerCase(), true);
                                plugin.saveConfig();
                            }
                            break;
                        case STRING:
                            plugin.config.set(split[0].toLowerCase(), split[1]);
                            plugin.saveConfig();
                            break;
                        case DOUBLE:
                            try {
                                double number = Double.parseDouble(split[1]);
                                plugin.config.set(split[0].toLowerCase(), number);
                                plugin.saveConfig();
                            } catch (NumberFormatException e) {
                                plugin.getLogger().warning("Unable to set " + split[0] + " to " + split[1] + " as it is not a double.");
                            }
                            break;
                        case FLOAT:
                            try {
                                float number = Float.parseFloat(split[1]);
                                plugin.config.set(split[0].toLowerCase(), number);
                                plugin.saveConfig();
                            } catch (NumberFormatException e) {
                                plugin.getLogger().warning("Unable to set " + split[0] + " to " + split[1] + " as it is not a float.");
                            }
                            break;
                        case INT:
                            try {
                                int number = Integer.parseInt(split[1]);
                                plugin.config.set(split[0].toLowerCase(), number);
                                plugin.saveConfig();
                            } catch (NumberFormatException e) {
                                plugin.getLogger().warning("Unable to set " + split[0] + " to " + split[1] + " as it is not an int.");
                            }
                            break;
                        case FORBIDDEN:
                            plugin.getLogger().warning("Enjin tried setting the value " + split[0] + " to " + split[1] + " but was forbidden!");
                    }
                } else {
                    plugin.getLogger().warning("Enjin tried setting the value " + split[0] + " to " + split[1] + " but it doesn't exist!");
                }
            }
            plugin.initFiles();
            //Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), PacketUtilities.readString(in));
        } catch (Throwable t) {
            Bukkit.getLogger().warning("Failed to set config variables via 0x15, " + t.getMessage());
            t.printStackTrace();
        }
    }
}
