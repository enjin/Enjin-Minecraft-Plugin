package com.enjin.officialplugin;

import org.bukkit.command.CommandSender;

public class CommandWrapper implements Comparable<CommandWrapper> {

    String command;
    CommandSender sender;
    long delay = 0;

    public CommandWrapper(CommandSender sender, String command) {
        this.sender = sender;
        this.command = command;
    }

    /**
     * Sets up a command that has a delay.
     *
     * @param sender  The command sender.
     * @param command The actual command to execute
     * @param delay   The time, in milliseconds, when it should execute.
     */
    public CommandWrapper(CommandSender sender, String command, long delay) {
        this.sender = sender;
        this.command = command;
        this.delay = delay;
    }

    public String getCommand() {
        return command;
    }

    public CommandSender getSender() {
        return sender;
    }

    /**
     * Gets the time in milliseconds when we need to execute the command.
     *
     * @return
     */
    public long getDelay() {
        return delay;
    }

    @Override
    public int compareTo(CommandWrapper o) {
        if (delay < o.delay) {
            return -1;
        } else if (delay > o.delay) {
            return 1;
        }
        return 0;
    }

    @Override
    public String toString() {
        return command + "\0" + delay;
    }

}
