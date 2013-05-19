package com.enjin.officialplugin.events;

import net.canarymod.hook.CancelableHook;

public class EnjinBanPlayerEvent extends CancelableHook {
    
    String[] players;
    
    public EnjinBanPlayerEvent(String[] players) {
		this.players = players;
	}

	public String[] getBannedPlayers() {
		return players;
	}
	
	public void setBannedPlayers(String[] players) {
		this.players = players;
	}
}
