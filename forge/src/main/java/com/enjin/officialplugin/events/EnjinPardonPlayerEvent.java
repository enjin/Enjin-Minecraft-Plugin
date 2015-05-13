package com.enjin.officialplugin.events;

public class EnjinPardonPlayerEvent extends Event {

    boolean iscanceled = false;

    String[] players;

    public EnjinPardonPlayerEvent(String[] players) {
        this.players = players;
    }

    public String[] getPardonedPlayers() {
        return players;
    }

    public void setPardonedPlayers(String[] players) {
        this.players = players;
    }

    public boolean isCancelled() {
        return iscanceled;
    }

    public void setCancelled(boolean cancel) {
        iscanceled = cancel;
    }
}
