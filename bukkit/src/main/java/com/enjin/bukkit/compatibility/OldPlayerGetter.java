package com.enjin.bukkit.compatibility;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class OldPlayerGetter implements OnlinePlayerGetter {

    @Override
    public Player[] getOnlinePlayers() {
        return Bukkit.getOnlinePlayers().toArray(new Player[Bukkit.getOnlinePlayers().size()]);
    }

}
