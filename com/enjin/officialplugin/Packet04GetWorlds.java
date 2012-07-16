package com.enjin.officialplugin;

import org.bukkit.Bukkit;
import org.bukkit.World;

public class Packet04GetWorlds implements Packet {
	@Override
	public void handle(ServerConnection con) {
		try {
			StringBuffer worlds = new StringBuffer();
			for(World world : Bukkit.getWorlds()) {
				worlds.append(world.getName());
				worlds.append(',');
			}
			worlds.deleteCharAt(worlds.length()-1);
			short length = (short) worlds.length();
			con.out.write(length);
			for(short s = 0; s<length; s++) {
				con.out.write(worlds.charAt(s));
			}
		} catch (Throwable t) {
			t.printStackTrace();
		}
	}
}
