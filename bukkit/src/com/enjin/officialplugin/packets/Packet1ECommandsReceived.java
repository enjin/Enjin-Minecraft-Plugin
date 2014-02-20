package com.enjin.officialplugin.packets;

import java.io.BufferedInputStream;
import java.io.IOException;

import com.enjin.officialplugin.EnjinMinecraftPlugin;
import com.enjin.officialplugin.events.HeadsUpdatedEvent;
import com.enjin.officialplugin.heads.HeadData;
import com.enjin.officialplugin.heads.HeadLocation.Type;

/**
 * 
 * @author OverCaste (Enjin LTE PTD).
 * This software is released under an Open Source license.
 * @copyright Enjin 2012.
 * 
 */

public class Packet1ECommandsReceived {
	
	public static void handle(BufferedInputStream in, EnjinMinecraftPlugin plugin) {
		try {
			String commandsreceived = PacketUtilities.readString(in);
			EnjinMinecraftPlugin.debug("Removing these command ids from the list: " + commandsreceived);
			String[] msg = commandsreceived.split(",");
			if((msg.length > 0)) {
				for(int i = 0; i < msg.length; i++) {
					plugin.removeCommandID(msg[i].trim());
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
