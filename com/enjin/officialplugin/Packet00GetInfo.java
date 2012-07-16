package com.enjin.officialplugin;

import java.io.FileInputStream;
import java.util.Properties;

import org.bukkit.Bukkit;


/**
 *MaxPlayer(0) (int)
 * - Number of maximum players allowed on server
 * Players(1) (int)
 * - The number of online players
 * Motd(2) (string)
 * - MOTD (Message of the day)
 * HasRanks(3) (boolean [1/0])
 * - Does the server have a permissions system enabled?
 */
public class Packet00GetInfo implements Packet {

	static final String SERVER_MOTD;
	static String message = null;
	
	static {
		String motd = "";
		FileInputStream in = null;
		try {
			Properties prop = new Properties();
			in = new FileInputStream("server.properties");
			prop.load(in);
			motd = prop.getProperty("motd");
		} catch (Throwable t) {
			t.printStackTrace();
		} finally {
			if(in != null) {
				try {
					in.close();
				} catch (Throwable t){};
			}
		}
		SERVER_MOTD = motd;
	}
	
	@Override
	public void handle(ServerConnection con) {
		try {
			byte id = (byte)(con.in.read() & 0xFF);
			//StringBuilder builder;
			switch(id) {
			case 0:
				message = String.valueOf(Bukkit.getMaxPlayers());
				break;
			case 1:
				message = String.valueOf(Bukkit.getOnlinePlayers().length);
				break;
			case 2:
				message = SERVER_MOTD;
				break;
			case 3:
				message = (EnjinMinecraftPlugin.permission == null) ? "FALSE" : "TRUE";
				break;
			case 4:
				message = Bukkit.getPluginManager().getPlugin("Enjin Minecraft Plugin").getDescription().getVersion();
				break;
			}
			if(message != null) {
				System.out.println("Writing! ID: " + id);
				System.out.println("Sending: " + message + ", (length: " + message.length() + ")");
				try {
					con.out.write(0);
					con.out.write(message.length());
					for(byte b : message.getBytes()) {
						con.out.write((short)(b&0xFF));
					}
				} catch (Throwable t) {
					t.printStackTrace();
				}
				return;
			}
			Bukkit.getLogger().warning("A connection failed to send the proper packet data for packet 0x00: " + id);
		} catch (Throwable t) {
			t.printStackTrace();
		}
	}
}
