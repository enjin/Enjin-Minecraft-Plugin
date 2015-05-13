package com.enjin.officialplugin.events;

import org.bukkit.OfflinePlayer;
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

    OfflinePlayer[] players;

    public RemoveWhitelistPlayersEvent(OfflinePlayer[] players) {
        super(true);
        this.players = players;
    }

    public OfflinePlayer[] getPlayers() {
        return players;
    }

}
