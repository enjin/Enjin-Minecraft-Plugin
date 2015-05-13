package com.enjin.officialplugin.events;


public class EnjinBanPlayerEvent extends Event {

    String[] players;
    boolean iscanceled = false;

    public EnjinBanPlayerEvent(String[] players) {
        this.players = players;
    }

    public String[] getBannedPlayers() {
        return players;
    }

    public void setBannedPlayers(String[] players) {
        this.players = players;
    }

    public boolean isCancelled() {
        return iscanceled;
    }

    public void setCancelled(boolean cancel) {
        iscanceled = cancel;
    }
}
