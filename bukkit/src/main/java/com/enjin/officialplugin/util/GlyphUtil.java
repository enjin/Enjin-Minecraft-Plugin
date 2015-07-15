package com.enjin.officialplugin.util;

import com.enjin.officialplugin.EnjinMinecraftPlugin;
import org.bukkit.map.MinecraftFont;

import java.io.IOException;
import java.io.InputStream;

public class GlyphUtil {
    private static byte[] glyphWidth;

    public static int getTextWidth(String text) {
        int width = 0;
        for (char c : text.toCharArray()) {
            width += getCharWidth(c);
        }
        return width;
    }

    /**
     * Returns the width of this character as rendered.
     */
    public static int getCharWidth(char c) {
        try {
            loadGlyphSizes();
        } catch (IOException e) {
            glyphWidth = null;
            EnjinMinecraftPlugin.instance.getLogger().severe("Unable to read width of unicode glyphs.");
            return 15;
        }

        if (c == 167) {
            return -1;
        } else if (c == 32) {
            return 4;
        } else {
            int i;
            if (isValidCharacter(c)) {
                i = c;
            } else {
                i = 69;
            }

            if (i >= 0 && i < 256 && MinecraftFont.Font.isValid(String.valueOf(c))) {
                return MinecraftFont.Font.getWidth(String.valueOf(c));
            } else if (glyphWidth[c] != 0) {
                int j = glyphWidth[c] >>> 4;
                int k = glyphWidth[c] & 15;

                if (k > 7) {
                    k = 15;
                    j = 0;
                }

                ++k;
                return (k - j) / 2 + 1;
            } else {
                return 0;
            }
        }
    }

    /**
     * Checks if a character is valid when rendered.
     */
    public static boolean isValidCharacter(char c) {
        return c >= 32 && c != 127 && c != 167;
    }

    private static void loadGlyphSizes() throws IOException {
        if (glyphWidth == null) {
            glyphWidth = new byte[65536];

            InputStream is = EnjinMinecraftPlugin.instance.getResource("glyph_sizes.bin");
            is.read(glyphWidth);
        }
    }
}
