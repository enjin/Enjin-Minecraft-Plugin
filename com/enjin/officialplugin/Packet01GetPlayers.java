package com.enjin.officialplugin;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class Packet01GetPlayers implements Packet {
	@Override
	public void handle(ServerConnection con) {
		StringBuilder builder = new StringBuilder();
		for(Player p : Bukkit.getOnlinePlayers()) {
			builder.append('\n');
			builder.append(p.getName());
		}
		if(builder.length() > 0) {
			builder.deleteCharAt(0);
		}
		String message = builder.toString();
		builder = null;
		try {
			con.out.write(message.length());
			for(byte b : message.getBytes()) {
				con.out.write((short)(b&0xFF));
			}
		} catch (Throwable t) {
			t.printStackTrace();
		}
	}
}
