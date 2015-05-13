package com.enjin.officialplugin.points;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;

public class PlayerUtil {

    public static EntityPlayer getPlayer(String playerName) {
        for (Object o : MinecraftServer.getServer().getConfigurationManager().playerEntityList) {
            EntityPlayer player = (EntityPlayer) o;
            if (player.getEntityName().equalsIgnoreCase(playerName)) {
                return player;
            }
        }
        return null;
    }
}
