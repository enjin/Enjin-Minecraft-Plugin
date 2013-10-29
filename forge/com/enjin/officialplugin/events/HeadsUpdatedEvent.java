package com.enjin.officialplugin.events;

import net.minecraftforge.event.Event;

import com.enjin.officialplugin.heads.HeadLocation;
import com.enjin.officialplugin.heads.HeadLocation.Type;

/**
 * This gets called whenever a head stat has gotten updated.
 * Custom plugins can use this to update their database of stats
 * for players.
 * @author Tux2
 *
 */
public class HeadsUpdatedEvent extends Event {
	
	HeadLocation.Type type;
	String itemId;
	
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
