package com.enjin.officialplugin.events;

import net.canarymod.hook.Hook;

public class RemoveWhitelistPlayersEvent extends Hook {

    String[] players;

    public RemoveWhitelistPlayersEvent(String[] players) {
        this.players = players;
    }

    public String[] getPlayers() {
        return players;
    }

}
