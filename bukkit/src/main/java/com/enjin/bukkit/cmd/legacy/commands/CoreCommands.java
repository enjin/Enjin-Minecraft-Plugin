package com.enjin.bukkit.cmd.legacy.commands;

import com.enjin.bukkit.cmd.legacy.Command;
import com.enjin.bukkit.config.EMPConfig;
import com.enjin.bukkit.util.PermissionsUtil;
import com.enjin.core.Enjin;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

public class CoreCommands {
    @Command(value = "enjin", aliases = "e", requireValidKey = false)
    public static void enjin(CommandSender sender, String[] args) {
        if (PermissionsUtil.hasPermission(sender, "enjin.sign.set")) {
            sender.sendMessage(ChatColor.GOLD + "/enjin heads: " + ChatColor.RESET + "Shows in game help for the heads and sign stats part of the plugin.");
        }

        if (PermissionsUtil.hasPermission(sender, "enjin.points.getself")) {
            sender.sendMessage(ChatColor.GOLD + "/enjin points: " + ChatColor.RESET + "Shows your current website points.");
        }

        if (PermissionsUtil.hasPermission(sender, "enjin.points.getothers")) {
            sender.sendMessage(ChatColor.GOLD + "/enjin points <NAME>: " + ChatColor.RESET + "Shows another player's current website points.");
        }

        if (PermissionsUtil.hasPermission(sender, "enjin.points.add")) {
            sender.sendMessage(ChatColor.GOLD + "/enjin addpoints <NAME> <AMOUNT>: " + ChatColor.RESET + "Add points to a player.");
        }

        if (PermissionsUtil.hasPermission(sender, "enjin.points.remove")) {
            sender.sendMessage(ChatColor.GOLD + "/enjin removepoints <NAME> <AMOUNT>: " + ChatColor.RESET + "Remove points from a player.");
        }

        if (PermissionsUtil.hasPermission(sender, "enjin.points.set")) {
            sender.sendMessage(ChatColor.GOLD + "/enjin setpoints <NAME> <AMOUNT>: " + ChatColor.RESET + "Set a player's total points.");
        }

        if (PermissionsUtil.hasPermission(sender, "enjin.support")) {
            sender.sendMessage(ChatColor.GOLD + "/enjin support: " + ChatColor.RESET + "Starts ticket session or informs player of available modules.");
        }

        if (PermissionsUtil.hasPermission(sender, "enjin.ticket.self")) {
            sender.sendMessage(ChatColor.GOLD + "/enjin ticket: " + ChatColor.RESET + "Sends player a list of their tickets.");
        }

        if (PermissionsUtil.hasPermission(sender, "enjin.ticket.open")) {
            sender.sendMessage(ChatColor.GOLD + "/enjin openticket: " + ChatColor.RESET + "Sends player a list of open tickets.");
        }

        if (PermissionsUtil.hasPermission(sender, "enjin.ticket.reply")) {
            sender.sendMessage(ChatColor.GOLD + "/enjin reply <module #> <ticket id> <message>: " + ChatColor.RESET + "Sends a reply to a ticket.");
        }

        if (PermissionsUtil.hasPermission(sender, "enjin.ticket.status")) {
            sender.sendMessage(ChatColor.GOLD + "/enjin ticketstatus <module #> <ticket id> <open|pending|closed>: " + ChatColor.RESET + "Sets the status of a ticket.");
        }

        sender.sendMessage(new StringBuilder(ChatColor.GOLD.toString())
                                   .append("/")
                                   .append(Enjin.getConfiguration(EMPConfig.class).getBuyCommand())
                                   .append(':')
                                   .append(ChatColor.RESET)
                                   .append(" Display items available for purchase.")
                                   .toString());
        sender.sendMessage(new StringBuilder(ChatColor.GOLD.toString())
                                   .append("/")
                                   .append(Enjin.getConfiguration(EMPConfig.class).getBuyCommand())
                                   .append(" page <#>:")
                                   .append(ChatColor.RESET)
                                   .append(" View the next page of results.")
                                   .toString());
        sender.sendMessage(new StringBuilder(ChatColor.GOLD.toString())
                                   .append("/")
                                   .append(Enjin.getConfiguration(EMPConfig.class).getBuyCommand())
                                   .append(" <ID>:")
                                   .append(ChatColor.RESET)
                                   .append(" Purchase the specified item ID in the server shop.")
                                   .toString());
    }
}
