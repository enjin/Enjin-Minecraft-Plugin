package com.enjin.officialplugin.compatibility;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class OldPlayerGetter implements OnlinePlayerGetter {

	@Override
	public Player[] getOnlinePlayers() {
		return Bukkit.getOnlinePlayers();
	}

}
