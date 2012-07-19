package com.enjin.officialplugin;

import java.io.IOException;

public class Packet10AddPlayerGroup implements Packet {
	@Override
	public void handle(ServerConnection con) {
		try {
			StringBuilder builder = new StringBuilder();
			short length = (short) con.in.read();
			for(short s = 0; s<length; s++) {
				builder.append((char)con.in.read());
			}
			System.out.println("Add player groups string: " + builder.toString());
			String[] msg = builder.toString().split(",");
			if((msg.length == 2) || (msg.length == 3)) {
				String playername = msg[0];
				String groupname = msg[1];
				String world = (msg.length == 3) ? msg[2] : null;
				EnjinMinecraftPlugin.permission.playerAddGroup(world, playername, groupname);
			} else {
				System.out.println("Enjin minecraft plugin sent invalid string: " + builder.toString());
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
