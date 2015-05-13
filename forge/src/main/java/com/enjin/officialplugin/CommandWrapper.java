package com.enjin.officialplugin;

import net.minecraft.command.ICommandSender;

public class CommandWrapper {

    String command;
    long delay;
    ICommandSender sender;

    public CommandWrapper(String command) {
        this.command = command;
    }

    public CommandWrapper(ICommandSender sender, String command, long delay) {
        this.sender = sender;
        this.command = command;
        this.delay = delay;
    }

    public String getCommand() {
        return command;
    }

    public long getDelay() {
        return delay;
    }

    public ICommandSender getSender() {
        return sender;
    }
}
