package com.enjin.officialplugin.events;

import org.bukkit.event.Cancellable;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * This gets called whenever a player is pardoned/unbanned by Enjin and can be canceled.
 * @author Tux2
 *
 */
public class EnjinPardonPlayerEvent extends Event implements Cancellable {
	
	// Custom Event Requirements
    private static final HandlerList handlers = new HandlerList();
    boolean iscanceled = false;
    
    public HandlerList getHandlers() {
        return handlers;
    }
     
    public static HandlerList getHandlerList() {
        return handlers;
    }
    
    String[] players;
    
    public EnjinPardonPlayerEvent(String[] players) {
    	super(true);
		this.players = players;
	}

	public String[] getPardonedPlayers() {
		return players;
	}
	
	public void setPardonedPlayers(String[] players) {
		this.players = players;
	}

	@Override
	public boolean isCancelled() {
		return iscanceled;
	}

	@Override
	public void setCancelled(boolean cancel) {
		iscanceled = cancel;
	}
}
