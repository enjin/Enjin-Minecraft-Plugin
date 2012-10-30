package com.enjin.officialplugin;

public class PlayerPerms {
	
	String playername = "";
	String worldname = "";
	
	public PlayerPerms(String player, String world) {
		playername = player;
		worldname = world;
	}
	
	public String getPlayerName() {
		return playername;
	}
	
	public String getWorldName() {
		return worldname;
	}
}
