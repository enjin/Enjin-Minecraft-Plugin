package com.enjin.officialplugin;

import java.io.IOException;

import org.bukkit.Bukkit;

public class PacketLoader {

	public static void readPacket(int read, ServerConnection con) throws UnknownPacketException {
		switch(read) {
		case 0x0:
			(new Packet00GetInfo()).handle(con);
			break;
		case 0x1:
			(new Packet01GetPlayers()).handle(con);
			break;
		case 0x2:
			(new Packet02GetGroups()).handle(con);
			break;
		case 0x3:
			(new Packet03GetPlayerGroups()).handle(con);
			break;
		case 0x4:
			(new Packet04GetWorlds()).handle(con);
			break;
		case 0x10:
			(new Packet10AddPlayerGroup()).handle(con);
			break;
		case 0x11:
			(new Packet11RemovePlayerGroup()).handle(con);
			break;
		default:
			throw new UnknownPacketException(read); 
		}
	}

	public static boolean handleLogin(ServerConnection con) throws Throwable {
		if(EnjinMinecraftPlugin.hash.equals("")) {
			con.out.write(3);
			return false;
		}
		int l1 = con.in.read();
		int l2 = con.in.read();
		if(l1 == -1 || l2 == -1) {
			con.out.write(1);
			return false;
		}
		int length = (int)((l1 << 8) + (l2 << 0));
		if(length != EnjinMinecraftPlugin.hash.length()) {
			con.out.write(1);
			return false;
		}
		StringBuilder hash = new StringBuilder();
		for(int i = 0; i<length; i++) {
			hash.append((char)con.in.read());
		}
		if(EnjinMinecraftPlugin.hash.equals(hash.toString())) {
			Bukkit.getLogger().info("An enjin server authenticated with the correct key.");
			con.out.write(0);
			return true;
		}
		con.out.write(2);
		Bukkit.getLogger().warning("A SUPPOSED ENJIN SERVER FAILED TO AUTHENTICATE! SENT KEY: " + hash + ".");
		return false;
	}
	
	public static String readString(ServerConnection con) throws IOException {
		int length = (int)((con.in.read() << 8) + (con.in.read() << 0));
		StringBuilder builder = new StringBuilder();
		for(int i = 0; i<length; i++) {
			builder.append((char)con.in.read());
		}
		return builder.toString();
	}
	
	public static void writeString(ServerConnection con, String s) throws IOException {
		int length = Math.min(s.length(), 65536);
	    con.out.write(length >>> 8 & 0xFF);
	    con.out.write(length >>> 0 & 0xFF);
	    for(int i = 0; i<length; i++) {
	    	con.out.write(s.charAt(i));
	    }
	}
}
