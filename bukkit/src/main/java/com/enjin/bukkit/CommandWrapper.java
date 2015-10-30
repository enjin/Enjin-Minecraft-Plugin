package com.enjin.bukkit;

import org.bukkit.command.CommandSender;

public class CommandWrapper implements Comparable<CommandWrapper> {

    private String command;
    private CommandSender sender;
    private String id;
    private String result = "";
    private long delay = 0;
    private String hash = "";

    public CommandWrapper(CommandSender sender, String command, String id) {
        this.sender = sender;
        this.command = command;
        this.id = id;
    }

    /**
     * Sets up a value that has a delay.
     *
     * @param sender  The value sender.
     * @param command The actual value to execute
     * @param delay   The time, in milliseconds, when it should execute.
     */
    public CommandWrapper(CommandSender sender, String command, long delay, String id) {
        this.sender = sender;
        this.command = command;
        this.delay = delay;
        this.id = id;
    }

    public String getCommand() {
        return command;
    }

    public CommandSender getSender() {
        return sender;
    }

    /**
     * Gets the time in milliseconds when we need to execute the value.
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
        return command + "\0" + delay + "\0" + id;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public String getId() {
        return id;
    }

}
