package com.enjin.officialplugin.packets;

import java.io.BufferedInputStream;
import java.io.IOException;

/**
 * @author OverCaste (Enjin LTE PTD).
 *         This software is released under an Open Source license.
 * @copyright Enjin 2012.
 */

public class PacketUtilities {
    public static String readString(BufferedInputStream in) throws IOException {
        StringBuilder builder = new StringBuilder();
        for (int c = in.read(); ((char) c) != '\r' && c != -1; c = in.read()) {
            builder.append(((char) c));
        }
        return builder.toString();
    }
}
