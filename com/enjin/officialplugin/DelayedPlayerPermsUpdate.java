package com.enjin.officialplugin;

import org.bukkit.entity.Player;

public class DelayedPlayerPermsUpdate implements Runnable {
	
	EMPListener listener;
	Player player;
	
	public DelayedPlayerPermsUpdate(EMPListener listener, Player player) {
		this.player = player;
		this.listener = listener;
	}

	@Override
	public void run() {
		listener.updatePlayerRanks(player);
	}

}
