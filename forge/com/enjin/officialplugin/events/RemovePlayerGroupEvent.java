package com.enjin.officialplugin.events;

public class RemovePlayerGroupEvent extends Event {
	    
    String player;
    String groupname;
    String world;
    
    public RemovePlayerGroupEvent(String player, String groupname, String world) {
		this.player = player;
		this.groupname = groupname;
		this.world = world;
	}

	public String getPlayer() {
		return player;
	}

	public String getGroupname() {
		return groupname;
	}

	public String getWorld() {
		return world;
	}

}
