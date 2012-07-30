package com.enjin.officialplugin;

import org.bukkit.Bukkit;
import org.bukkit.World;

public class Packet04GetWorlds implements Packet {
	@Override
	public void handle(ServerConnection con) {
		try {
			StringBuffer worlds = new StringBuffer();
			for(World world : Bukkit.getWorlds()) {
				worlds.append(',');
				worlds.append(world.getName());
			}
			worlds.deleteCharAt(0);
			PacketLoader.writeString(con, worlds.toString());
		} catch (Throwable t) {
			t.printStackTrace();
		}
	}
}
