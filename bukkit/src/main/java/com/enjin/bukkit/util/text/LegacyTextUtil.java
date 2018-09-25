package com.enjin.bukkit.util.text;

import net.kyori.text.format.TextColor;

public class LegacyTextUtil {

    public static final char CHARACTER = '\u00A7';

    public static TextColor getColor(String text) {
        TextColor color = TextColor.WHITE;
        if (text != null && text.length() <= 2) {
            color = getColor(text.charAt(text.length() == 1 ? 0 : 1));
        }
        return color;
    }

    public static TextColor getColor(char character) {
        TextColor color = TextColor.WHITE;
        for (TextColor c : TextColor.values()) {
            if (c.legacy() == character) {
                color = c;
                break;
            }
        }
        return color;
    }

    public static String getLegacyText(String text) {
        TextColor color = getColor(text);
        return new String(new char[] {CHARACTER, color.legacy()});
    }

}
