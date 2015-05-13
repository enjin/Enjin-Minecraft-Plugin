package com.enjin.officialplugin.compatibility;

import java.util.Iterator;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

/*
 * This class needs to be in a separate project with the 1.7.10 jar
 * along with OnlinePlayerGetter in order to compile properly.
 */
public class NewPlayerGetter implements OnlinePlayerGetter {

    @Override
    public Player[] getOnlinePlayers() {
        Iterator<? extends Player> players = Bukkit.getOnlinePlayers().iterator();
        Player[] oldplayers = new Player[Bukkit.getOnlinePlayers().size()];
        for (int i = 0; i < oldplayers.length; i++) {
            oldplayers[i] = players.next();
        }
        return oldplayers;
    }

}
