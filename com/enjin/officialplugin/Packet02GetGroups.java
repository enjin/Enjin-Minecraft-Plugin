package com.enjin.officialplugin;

public class Packet02GetGroups implements Packet {
	
	@Override
	public void handle(ServerConnection con) {
		try {
			StringBuffer groups = new StringBuffer();
			for(String group : EnjinMinecraftPlugin.permission.getGroups()) {
				groups.append(group);
				groups.append(',');
			}
			groups.deleteCharAt(groups.length()-1);
			short length = (short) groups.length();
			con.out.write(length);
			for(short s = 0; s<length; s++) {
				con.out.write(groups.charAt(s));
			}
		} catch (Throwable t) {
			t.printStackTrace();
		}
	}
}
