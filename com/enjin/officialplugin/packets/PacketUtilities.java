package com.enjin.officialplugin.packets;

import java.io.IOException;
import java.io.InputStream;

/**
 * 
 * @author OverCaste (Enjin LTE PTD).
 * This software is released under an Open Source license.
 * @copyright Enjin 2012.
 * 
 */

public class PacketUtilities {
	public static String readString(InputStream in) throws IOException {
		StringBuilder builder = new StringBuilder();
		for(char c = (char) in.read(); c != '\r' ; c = (char) in.read()) {
			builder.append(c);
		}
		return builder.toString();
	}
}
