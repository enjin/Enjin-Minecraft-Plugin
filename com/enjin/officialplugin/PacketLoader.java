package com.enjin.officialplugin;

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
		int length = con.in.read();
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

}
