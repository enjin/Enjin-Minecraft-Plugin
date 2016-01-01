package com.enjin.sponge.utils.text;

import com.enjin.sponge.EnjinMinecraftPlugin;
import com.google.common.collect.Maps;
import org.spongepowered.api.text.Text;

import java.util.Map;
import java.util.regex.Pattern;

public class TextUtils {
    private static final Pattern STRIP_COLOR_PATTERN = Pattern.compile("(?i)\\u00A7[0-9A-FK-OR]");
    private static final Map<Character, Pattern> alternateStripColorPatterns = Maps.newHashMap();
    public static final int MINECRAFT_CONSOLE_WIDTH = 320;

    public static int getWidth(String text) {
        if (text == null || text.isEmpty()) {
            return 0;
        }

        text = stripCodes('&', text);

        return GlyphUtil.getTextWidth(text) + (text.length() - 1);
    }

    public static String trim(String text, String ellipses) {
        String output = text;
        EnjinMinecraftPlugin.getInstance().debug("Text Width: " + getWidth(output));
        if (getWidth(output) > MINECRAFT_CONSOLE_WIDTH) {
            output = output.substring(0, output.length() - 1);

            while (getWidth(output + (ellipses == null ? "" : ellipses)) > MINECRAFT_CONSOLE_WIDTH) {
                output = output.substring(0, output.length() - 1);
            }

            return output + (ellipses == null ? "" : ellipses);
        } else {
            return output;
        }
    }

    public static String stripCodes(String input) {
        return input == null ? null : STRIP_COLOR_PATTERN.matcher(input).replaceAll("");
    }

    public static String stripCodes(char alt, String input) {
        Pattern pattern;

        if (alternateStripColorPatterns.containsKey(alt)) {
            pattern = alternateStripColorPatterns.get(alt);
        } else {
            pattern = Pattern.compile("(?i)" + alt + "[0-9A-FK-OR]");
            alternateStripColorPatterns.put(alt, pattern);
        }

        if (input == null) {
            return null;
        }

        String start = stripCodes(input);
        return start == null ? null : pattern.matcher(start).replaceAll("");
    }

    public static String translateAlternateColorCodes(String input, char alt) {
        char[] b = input.toCharArray();

        for(int i = 0; i < b.length - 1; ++i) {
            if(b[i] == alt && "0123456789AaBbCcDdEeFfKkLlMmNnOoRr".indexOf(b[i + 1]) > -1) {
                b[i] = 167;
                b[i + 1] = Character.toLowerCase(b[i + 1]);
            }
        }

        return new String(b);
    }

}
