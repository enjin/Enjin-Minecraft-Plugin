package com.enjin.officialplugin.events;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class AddWhitelistPlayersEvent extends Event {
	
	// Custom Event Requirements
    private static final HandlerList handlers = new HandlerList();
    
    public HandlerList getHandlers() {
        return handlers;
    }
     
    public static HandlerList getHandlerList() {
        return handlers;
    }
    
    String[] players;
    
    public AddWhitelistPlayersEvent(String[] players) {
    	super(true);
		this.players = players;
	}

	public String[] getPlayers() {
		return players;
	}

}
