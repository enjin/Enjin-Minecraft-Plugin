package com.enjin.bukkit.command.commands;

import com.enjin.bukkit.EnjinMinecraftPlugin;
import com.enjin.bukkit.command.Directive;
import com.enjin.bukkit.command.Permission;
import com.enjin.bukkit.stats.StatsPlayer;
import com.enjin.bukkit.stats.WriteStats;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

public class StatCommands {
    @Permission(permission = "enjin.playerstats")
    @Directive(parent = "enjin", directive = "playerstats")
    public static void playerStats(CommandSender sender, String[] args) {
        EnjinMinecraftPlugin plugin = EnjinMinecraftPlugin.instance;

        if (args.length == 1) {
            StatsPlayer player = null;
            String index = args[0].toLowerCase();
            if (plugin.supportsUUID()) {
                OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(index);
                UUID uuid = offlinePlayer.getUniqueId();

                if (plugin.playerstats.containsKey(uuid.toString().toLowerCase())) {
                    player = plugin.playerstats.get(uuid.toString());
                }
            } else if (plugin.playerstats.containsKey(index)) {
                player = plugin.playerstats.get(index);
            }

            if (player != null) {
                sender.sendMessage(ChatColor.DARK_GREEN + "Player stats for player: " + ChatColor.GOLD + player.getName());
                sender.sendMessage(ChatColor.DARK_GREEN + "Deaths: " + ChatColor.GOLD + player.getDeaths());
                sender.sendMessage(ChatColor.DARK_GREEN + "Kills: " + ChatColor.GOLD + player.getKilled());
                sender.sendMessage(ChatColor.DARK_GREEN + "Blocks broken: " + ChatColor.GOLD + player.getBrokenblocks());
                sender.sendMessage(ChatColor.DARK_GREEN + "Blocks placed: " + ChatColor.GOLD + player.getPlacedblocks());
                sender.sendMessage(ChatColor.DARK_GREEN + "Block types broken: " + ChatColor.GOLD + player.getBrokenblocktypes().toString());
                sender.sendMessage(ChatColor.DARK_GREEN + "Block types placed: " + ChatColor.GOLD + player.getPlacedblocktypes().toString());
                sender.sendMessage(ChatColor.DARK_GREEN + "Foot distance traveled: " + ChatColor.GOLD + player.getFootdistance());
                sender.sendMessage(ChatColor.DARK_GREEN + "Boat distance traveled: " + ChatColor.GOLD + player.getBoatdistance());
                sender.sendMessage(ChatColor.DARK_GREEN + "Minecart distance traveled: " + ChatColor.GOLD + player.getMinecartdistance());
                sender.sendMessage(ChatColor.DARK_GREEN + "Pig distance traveled: " + ChatColor.GOLD + player.getPigdistance());
            } else {
                sender.sendMessage("I'm sorry, but I couldn't find a player with stats with that name.");
            }
        } else {
            sender.sendMessage("USAGE: /enjin playerstats <player>");
        }
    }

    @Permission(permission = "enjin.savestats")
    @Directive(parent = "enjin", directive = "savestats")
    public static void saveStats(CommandSender sender, String[] args) {
        new WriteStats(EnjinMinecraftPlugin.instance).write("stats.stats");
        sender.sendMessage(ChatColor.GREEN + "Stats saved to stats.stats.");
    }

    @Permission(permission = "enjin.serverstats")
    @Directive(parent = "enjin", directive = "serverstats")
    public static void serverStats(CommandSender sender, String[] args) {
        EnjinMinecraftPlugin plugin = EnjinMinecraftPlugin.instance;
        Date date = new Date(plugin.serverstats.getLastserverstarttime());
        DateFormat format = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss z");

        sender.sendMessage(ChatColor.DARK_GREEN + "Server Stats");
        sender.sendMessage(ChatColor.DARK_GREEN + "Server Start time: " + ChatColor.GOLD + format.format(date));
        sender.sendMessage(ChatColor.DARK_GREEN + "Total number of creeper explosions: " + ChatColor.GOLD + plugin.serverstats.getCreeperexplosions());
        sender.sendMessage(ChatColor.DARK_GREEN + "Total number of kicks: " + ChatColor.GOLD + plugin.serverstats.getTotalkicks());
        sender.sendMessage(ChatColor.DARK_GREEN + "Kicks per player: " + ChatColor.GOLD + plugin.serverstats.getPlayerkicks().toString());
    }
}
