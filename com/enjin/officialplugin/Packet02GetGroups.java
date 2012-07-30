package com.enjin.officialplugin;

public class Packet02GetGroups implements Packet {
	
	@Override
	public void handle(ServerConnection con) {
		try {
			StringBuffer groups = new StringBuffer();
			for(String group : EnjinMinecraftPlugin.permission.getGroups()) {
				groups.append(',');
				groups.append(group);
			}
			if(groups.length() > 0) {
				groups.deleteCharAt(0);
			}
			PacketLoader.writeString(con, groups.toString());
		} catch (Throwable t) {
			t.printStackTrace();
		}
	}
}
