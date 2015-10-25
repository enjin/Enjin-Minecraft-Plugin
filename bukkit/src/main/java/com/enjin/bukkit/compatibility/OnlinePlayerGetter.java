package com.enjin.bukkit.compatibility;

import org.bukkit.entity.Player;

/*
 * This class needs to be in a separate project with the 1.7.10 jar
 * along with NewPlayerGetter in order to compile properly.
 */
public interface OnlinePlayerGetter {

    abstract Player[] getOnlinePlayers();

}
