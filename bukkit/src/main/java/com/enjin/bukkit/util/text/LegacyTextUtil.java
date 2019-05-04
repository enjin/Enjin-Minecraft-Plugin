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
            if (getLegacyColorChar(c) == character) {
                color = c;
                break;
            }
        }
        return color;
    }

    public static String getLegacyText(String text) {
        TextColor color = getColor(text);
        return new String(new char[]{CHARACTER, getLegacyColorChar(color)});
    }

    public static Character getLegacyColorChar(TextColor color) {
        switch (color) {
            case BLACK:
                return '0';
            case DARK_BLUE:
                return '1';
            case DARK_GREEN:
                return '2';
            case DARK_AQUA:
                return '3';
            case DARK_RED:
                return '4';
            case DARK_PURPLE:
                return '5';
            case GOLD:
                return '6';
            case GRAY:
                return '7';
            case DARK_GRAY:
                return '8';
            case BLUE:
                return '9';
            case GREEN:
                return 'a';
            case AQUA:
                return 'b';
            case RED:
                return 'c';
            case LIGHT_PURPLE:
                return 'd';
            case YELLOW:
                return 'e';
            case WHITE:
                return 'f';
            default:
                return null;
        }
    }

    public static TextColor getTextColor(char character) {
        switch (character) {
            case '0':
                return TextColor.BLACK;
            case '1':
                return TextColor.DARK_BLUE;
            case '2':
                return TextColor.DARK_GREEN;
            case '3':
                return TextColor.DARK_AQUA;
            case '4':
                return TextColor.DARK_RED;
            case '5':
                return TextColor.DARK_PURPLE;
            case '6':
                return TextColor.GOLD;
            case '7':
                return TextColor.GRAY;
            case '8':
                return TextColor.DARK_GRAY;
            case '9':
                return TextColor.BLUE;
            case 'a':
                return TextColor.GREEN;
            case 'b':
                return TextColor.AQUA;
            case 'c':
                return TextColor.RED;
            case 'd':
                return TextColor.LIGHT_PURPLE;
            case 'e':
                return TextColor.YELLOW;
            case 'f':
                return TextColor.WHITE;
            default:
                return null;
        }
    }

}
