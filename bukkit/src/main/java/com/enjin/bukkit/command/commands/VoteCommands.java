package com.enjin.bukkit.command.commands;

import com.enjin.bukkit.EnjinMinecraftPlugin;
import com.enjin.bukkit.command.Directive;
import com.enjin.bukkit.command.Permission;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;

public class VoteCommands {
    @Permission(value = "enjin.test.vote")
    @Directive(parent = "enjin", value = "vote")
    public static void vote(CommandSender sender, String[] args) {
        EnjinMinecraftPlugin plugin = EnjinMinecraftPlugin.getInstance();

        if (args.length != 2) {
            sender.sendMessage("Usage: /enjin vote <username> <list>");
            return;
        }

        String username = args[0];
        String listname = args[1].replaceAll("[^0-9A-Za-z.\\-]", "");

        OfflinePlayer player = Bukkit.getOfflinePlayer(username);
        if (player != null) {
            username = username.concat("|" + player.getUniqueId().toString());
        }

        if (!plugin.getPlayerVotes().containsKey(listname)) {
            plugin.getPlayerVotes().put(listname, new ArrayList<>());
        }

        plugin.getPlayerVotes().get(listname).add(new Object[]{username, System.currentTimeMillis() / 1000});
        sender.sendMessage(ChatColor.GREEN + "You just added a vote for player " + username + " on list " + listname);
    }
}
