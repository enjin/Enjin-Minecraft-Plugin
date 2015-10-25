package com.enjin.bukkit.events;

import org.bukkit.OfflinePlayer;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * This gets called whenever a group is removed by Enjin.
 *
 * @author Tux2
 */
public class RemovePlayerGroupEvent extends Event {

    // Custom Event Requirements
    private static final HandlerList handlers = new HandlerList();

    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    OfflinePlayer player;
    String groupname;
    String world;

    public RemovePlayerGroupEvent(OfflinePlayer player, String groupname, String world) {
        super(true);
        this.player = player;
        this.groupname = groupname;
        this.world = world;
    }

    @Deprecated
    public String getPlayer() {
        return player.getName();
    }

    public OfflinePlayer getOfflinePlayer() {
        return player;
    }

    public String getGroupname() {
        return groupname;
    }

    public String getWorld() {
        return world;
    }

}
