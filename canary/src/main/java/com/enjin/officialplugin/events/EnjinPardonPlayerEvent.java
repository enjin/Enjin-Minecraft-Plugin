package com.enjin.officialplugin.events;

import net.canarymod.hook.CancelableHook;

public class EnjinPardonPlayerEvent extends CancelableHook {

    String[] players;

    public EnjinPardonPlayerEvent(String[] players) {
        this.players = players;
    }

    public String[] getPardonedPlayers() {
        return players;
    }

    public void setPardonedPlayers(String[] players) {
        this.players = players;
    }
}
