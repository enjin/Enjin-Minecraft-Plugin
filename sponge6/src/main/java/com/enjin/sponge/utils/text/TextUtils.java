package com.enjin.sponge.utils.text;

import com.enjin.core.Enjin;
import com.google.common.collect.Maps;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.serializer.TextSerializers;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class TextUtils {
    private static final Pattern                 STRIP_COLOR_PATTERN         = Pattern.compile("(?i)\\u00A7[0-9A-FK-OR]");
    private static final Map<Character, Pattern> alternateStripColorPatterns = Maps.newHashMap();
    public static final  int                     MINECRAFT_CONSOLE_WIDTH     = 320;

    public static int getWidth(String text) {
        if (text == null || text.isEmpty()) {
            return 0;
        }

        text = stripCodes('&', text);

        return GlyphUtil.getTextWidth(text) + (text.length() - 1);
    }

    public static String trim(String text, String ellipses) {
        String output = text;
        Enjin.getLogger().debug("Text Width: " + getWidth(output));
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

    public static Text translateText(String text) {
        return TextSerializers.FORMATTING_CODE.deserializeUnchecked(text);
    }

    public static List<Text> splitToListWithPrefix(String text, int length, String prefix) {
        List<String> stringResult = new ArrayList<>();
        String[]     parts        = text.split(" ");

        StringBuilder builder = new StringBuilder();
        for (String part : parts) {
            if (builder.length() + part.length() > length) {
                stringResult.add(prefix + builder.toString().trim());
                builder = new StringBuilder();
            }

            if (builder.length() > 0) {
                builder.append(' ');
            }

            builder.append(part);
        }

        if (builder.length() > 0) {
            stringResult.add(prefix + builder.toString().trim());
        }

        return stringResult.stream()
                           .map(t -> TextSerializers.FORMATTING_CODE.deserialize(t))
                           .collect(Collectors.toList());
    }

}
