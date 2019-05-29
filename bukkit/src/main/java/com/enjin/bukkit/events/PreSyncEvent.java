package com.enjin.bukkit.events;

import lombok.Getter;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import java.util.Map;

public class PreSyncEvent extends Event {

    private static final HandlerList HANDLERS = new HandlerList();

    @Getter
    private Map<String, Object> status;

    public PreSyncEvent(Map<String, Object> status) {
        super(true);
        this.status = status;
    }

    @Override
    public HandlerList getHandlers() {
        return HANDLERS;
    }

    public static HandlerList getHandlerList() {
        return HANDLERS;
    }

}
