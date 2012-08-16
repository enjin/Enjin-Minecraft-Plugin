package com.enjin.officialplugin;

import java.io.IOException;
import java.io.InputStream;

/**
 * 
 * @author OverCaste (Enjin LTE PTD).
 * This software is released under an Open Source license.
 * @copyright Enjin 2012.
 * 
 */

public class Packet10AddPlayerGroup {
	
	public static void handle(InputStream in) {
		try {
			String[] msg = PacketUtilities.readString(in).split(",");
			if((msg.length == 2) || (msg.length == 3)) {
				String playername = msg[0];
				String groupname = msg[1];
				String world = (msg.length == 3) ? msg[2] : null;
				EnjinMinecraftPlugin.permission.playerAddGroup(world, playername, groupname);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
