package com.enjin.officialplugin.events;

public class RemoveWhitelistPlayersEvent extends Event {
	    
    String[] players;
    
    public RemoveWhitelistPlayersEvent(String[] players) {
		this.players = players;
	}

	public String[] getPlayers() {
		return players;
	}

}
