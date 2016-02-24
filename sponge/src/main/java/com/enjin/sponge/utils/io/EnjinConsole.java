package com.enjin.sponge.utils.io;

import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import java.util.regex.Pattern;

/**
 * Handling misc chat/console functions
 */
public class EnjinConsole {
    private static Pattern chatColorPattern = Pattern.compile("(?i)&([0-9A-FK-R])");

    public static Text header() {
        return Text.builder("=== Enjin Minecraft Plugin ===").color(TextColors.GREEN).build();
    }

    public static String translateColorCodes(String string) {
        if (string == null) {
            return "";
        }

        return chatColorPattern.matcher(string).replaceAll("\u00A7$1");
    }
}
