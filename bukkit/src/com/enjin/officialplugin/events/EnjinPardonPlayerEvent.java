package com.enjin.officialplugin.events;

import org.bukkit.OfflinePlayer;
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
    
    OfflinePlayer[] players;
    
    public EnjinPardonPlayerEvent(OfflinePlayer[] players) {
    	super(true);
		this.players = players;
	}

	public OfflinePlayer[] getPardonedPlayers() {
		return players;
	}
	
	public void setPardonedPlayers(OfflinePlayer[] players) {
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
