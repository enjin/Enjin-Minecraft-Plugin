package com.enjin.officialplugin.threaded;

import org.bukkit.entity.Player;

import com.enjin.officialplugin.EMPListener;

public class DelayedPlayerPermsUpdate implements Runnable {
	
	EMPListener listener;
	String player;
	
	@Deprecated
	public DelayedPlayerPermsUpdate(EMPListener listener, Player player) {
		this.player = player.getName();
		this.listener = listener;
	}
	
	public DelayedPlayerPermsUpdate(EMPListener listener, String player) {
		this.player = player;
		this.listener = listener;
	}

	@Override
	public void run() {
		listener.updatePlayerRanks(player);
	}

}
