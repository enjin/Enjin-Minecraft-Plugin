package com.enjin.officialplugin.events;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import com.enjin.officialplugin.heads.HeadLocation;
import com.enjin.officialplugin.heads.HeadLocation.Type;

public class HeadsUpdatedEvent extends Event {
	
	// Custom Event Requirements
    private static final HandlerList handlers = new HandlerList();
    
    private String itemId = "";
    
    public HandlerList getHandlers() {
        return handlers;
    }
     
    public static HandlerList getHandlerList() {
        return handlers;
    }
	
	HeadLocation.Type type;
	
	public HeadsUpdatedEvent(HeadLocation.Type type) {
		this.type = type;
	}

	public HeadsUpdatedEvent(Type type, String itemId) {
		super();
		this.type = type;
		this.itemId = itemId;
	}

	public HeadLocation.Type getType() {
		return type;
	}

	public String getItemId() {
		return itemId;
	}
	
	
}
