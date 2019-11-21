package com.enjin.bukkit.enums;

import org.bukkit.command.CommandSender;

public enum Permission {

    CMD_BROADCAST("broadcast"),
    CMD_DEBUG("debug"),
    CMD_KEY("setkey"),
    CMD_MESSAGE("inform"),
    CMD_HELP("help");

    private String node;

    Permission(String node) {
        this.node = String.format("enjin.%s", node);
    }

    public String node() {
        return node;
    }

    public boolean hasPermission(CommandSender sender) {
        return sender.hasPermission(node);
    }
}
