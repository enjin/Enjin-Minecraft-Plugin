package com.enjin.officialplugin.events;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * This gets called whenever a player is removed from the whitelist by Enjin.
 *
 * @author Tux2
 */
public class RemoveWhitelistPlayersEvent extends Event {

    // Custom Event Requirements
    private static final HandlerList handlers = new HandlerList();

    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    String[] players;

    public RemoveWhitelistPlayersEvent(String[] players) {
        super(true);
        this.players = players;
    }

    public String[] getPlayers() {
        return players;
    }

}
