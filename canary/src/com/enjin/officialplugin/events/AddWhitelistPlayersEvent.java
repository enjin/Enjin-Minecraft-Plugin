package com.enjin.officialplugin.events;

import net.canarymod.hook.Hook;

public class AddWhitelistPlayersEvent extends Hook {
    
    String[] players;
    
    public AddWhitelistPlayersEvent(String[] players) {
		this.players = players;
	}

	public String[] getPlayers() {
		return players;
	}

}
