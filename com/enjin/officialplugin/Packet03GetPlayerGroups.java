package com.enjin.officialplugin;

public class Packet03GetPlayerGroups implements Packet {

	String name;
	String world;
	
	@Override
	public void handle(ServerConnection con) {
		try {
			int slength = con.in.read();
			StringBuilder builder = new StringBuilder();
			System.out.println("Name length: " + slength);
			for(short s = 0; s<slength; s++) {
				builder.append((char)con.in.read());
			}
			name = builder.toString();
			slength = con.in.read();
			builder = new StringBuilder();
			System.out.println("World length: " + slength);
			for(short s = 0; s<slength; s++) {
				builder.append((char)con.in.read());
			}
			world = builder.toString();
			try {
				StringBuilder groups = new StringBuilder();
				System.out.println("Player: " + name);
				System.out.println("World: " + world);
				try {
					for(String group : EnjinMinecraftPlugin.permission.getPlayerGroups(world, name)) {
						groups.append(',');
						groups.append(group);
					}
				} catch (Throwable t) {} //known npe. Bug vault.
				if(groups.length() > 0) {
					groups.deleteCharAt(0);
				}
				short length = (short) groups.length();
				con.out.write(length);
				for(short s = 0; s<length; s++) {
					con.out.write(groups.charAt(s));
				}
			} catch (Throwable t) {
				t.printStackTrace();
			}
		} catch (Throwable t) {
			t.printStackTrace();
		}
	}
}
