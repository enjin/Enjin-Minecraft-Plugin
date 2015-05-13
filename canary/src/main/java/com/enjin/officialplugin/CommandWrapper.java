package com.enjin.officialplugin;

import net.canarymod.api.entity.living.humanoid.Player;

public class CommandWrapper {

    String command;
    Player sender;

    public CommandWrapper(Player sender, String command) {
        this.sender = sender;
        this.command = command;
    }

    public String getCommand() {
        return command;
    }

    public Player getSender() {
        return sender;
    }

}
