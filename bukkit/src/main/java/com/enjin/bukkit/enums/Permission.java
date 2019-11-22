package com.enjin.bukkit.enums;

import org.bukkit.command.CommandSender;

public enum Permission {

    CMD_BROADCAST("broadcast"),
    CMD_BUY("buy"),
    CMD_CONFIG("config"),
    CMD_DEBUG("debug"),
    CMD_HELP("help"),
    CMD_KEY("setkey"),
    CMD_LAG("lag"),
    CMD_MESSAGE("inform"),
    CMD_POINTS("points"),
    CMD_POINTS_ADD("points.add"),
    CMD_POINTS_GET("points.get"),
    CMD_POINTS_GET_OTHER("points.get.other"),
    CMD_POINTS_GET_SELF("points.get.self"),
    CMD_POINTS_REMOVE("points.remove"),
    CMD_POINTS_SET("points.set"),
    CMD_PUSH("push"),
    CMD_REPORT("report"),
    CMD_SIGN("sign"),
    CMD_SIGN_SET("sign.set"),
    CMD_SIGN_UPDATE("sign.update"),
    CMD_SUPPORT("support"),
    CMD_TEST_VOTE("test.vote"),
    CMD_TICKET("ticket"),
    CMD_TICKET_CREATE("ticket.create"),
    CMD_TICKET_OPEN("ticket.open"),
    CMD_TICKET_PRIVATE("ticket.private"),
    CMD_TICKET_REPLY("ticket.reply"),
    CMD_TICKET_STATUS("ticket.status"),
    CMD_TAGS("tags"),;

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
