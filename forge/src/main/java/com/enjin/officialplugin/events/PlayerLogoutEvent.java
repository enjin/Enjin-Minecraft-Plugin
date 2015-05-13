package com.enjin.officialplugin.events;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.event.entity.player.PlayerEvent;

public class PlayerLogoutEvent extends PlayerEvent {

    public PlayerLogoutEvent(EntityPlayer player) {
        super(player);
    }

    @Override
    public boolean isCancelable() {
        return false;
    }
}
