package com.enjin.officialplugin.events;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class RemovePlayerGroupEvent extends Event {
	
	// Custom Event Requirements
    private static final HandlerList handlers = new HandlerList();
    
    public HandlerList getHandlers() {
        return handlers;
    }
     
    public static HandlerList getHandlerList() {
        return handlers;
    }
    
    String player;
    String groupname;
    String world;
    
    public RemovePlayerGroupEvent(String player, String groupname, String world) {
    	super(true);
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
