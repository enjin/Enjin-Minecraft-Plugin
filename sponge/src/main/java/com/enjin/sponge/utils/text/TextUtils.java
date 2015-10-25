package com.enjin.sponge.utils.text;

import com.enjin.sponge.EnjinMinecraftPlugin;
import org.spongepowered.api.text.Texts;

public class TextUtils {
    public static final int MINECRAFT_CONSOLE_WIDTH = 320;

    public static int getWidth(String text) {
        if (text == null || text.isEmpty()) {
            return 0;
        }

        text = Texts.stripCodes(text, '&');
        text = Texts.stripCodes(text);

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
}
