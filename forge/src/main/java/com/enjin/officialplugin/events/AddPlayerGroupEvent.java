package com.enjin.officialplugin.events;

public class AddPlayerGroupEvent extends Event {

    String player;
    String groupname;
    String world;

    public AddPlayerGroupEvent(String player, String groupname, String world) {
        this.player = player;
        this.groupname = groupname;
        this.world = world;
    }

    public String getPlayer() {
        return player;
    }

    public String getGroupname() {
        return groupname;
    }

    public String getWorld() {
        return world;
    }

}
