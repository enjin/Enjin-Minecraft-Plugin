package com.enjin.bukkit.events;

import org.bukkit.OfflinePlayer;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Called when a group gets added to a player by the Enjin website.
 *
 * @author Tux2
 */
public class AddPlayerGroupEvent extends Event {

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

    public AddPlayerGroupEvent(OfflinePlayer player, String groupname, String world) {
        super(true);
        this.player = player;
        this.groupname = groupname;
        this.world = world;
    }

    /**
     * Gets the player the group was added on.
     *
     * @return The player name.
     */
    @Deprecated
    public String getPlayer() {
        return player.getName();
    }

    public OfflinePlayer getOfflinePlayer() {
        return player;
    }

    /**
     * Gets the group name to be added to the player.
     *
     * @return The Group name.
     */
    public String getGroupname() {
        return groupname;
    }

    /**
     * Gets the world name, or * if all worlds, the group is being added to.
     *
     * @return The world name.
     */
    public String getWorld() {
        return world;
    }

}
