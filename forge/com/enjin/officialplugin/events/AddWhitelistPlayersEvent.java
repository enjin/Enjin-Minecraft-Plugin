package com.enjin.officialplugin.events;

public class AddWhitelistPlayersEvent extends Event {
    
    String[] players;
    
    public AddWhitelistPlayersEvent(String[] players) {
		this.players = players;
	}

	public String[] getPlayers() {
		return players;
	}

}
