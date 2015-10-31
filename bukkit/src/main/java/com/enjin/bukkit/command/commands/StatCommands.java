package com.enjin.bukkit.command.commands;

import com.enjin.bukkit.EnjinMinecraftPlugin;
import com.enjin.bukkit.command.Directive;
import com.enjin.bukkit.command.Permission;
import com.enjin.bukkit.managers.StatsManager;
import com.enjin.bukkit.stats.StatsPlayer;
import com.enjin.bukkit.stats.WriteStats;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class StatCommands {
    @Permission("enjin.customstat")
    @Directive(parent = "enjin", value = "customstat")
    public static void customStat(CommandSender sender, String[] args) {
        if (args.length == 5) {
            String player = args[0].trim();
            String plugin = args[1].trim();
            String statName = args[2].trim();
            String statValue = args[3].trim();
            String cumulative = args[4].trim();
            boolean existing = cumulative.equalsIgnoreCase("true");
            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(player);
            StatsPlayer statsPlayer = StatsManager.getPlayerStats(offlinePlayer);

            try {
                statsPlayer.addCustomStat(plugin, statName, statValue.indexOf(".") > -1 ? Double.parseDouble(statValue) : Integer.parseInt(statValue), existing);
                sender.sendMessage(ChatColor.GREEN + "Successfully set the custom value!");
            } catch (NumberFormatException e) {
                sender.sendMessage(ChatColor.RED + "I'm sorry, custom values can only be numerical.");
            }
        } else {
            sender.sendMessage(ChatColor.DARK_RED + "Usage: /enjin customstat <player> <plugin> <stat-name> <stat-value> <cumulative>");
        }
    }

    @Permission(value = "enjin.playerstats")
    @Directive(parent = "enjin", value = "playerstats")
    public static void playerStats(CommandSender sender, String[] args) {
        EnjinMinecraftPlugin plugin = EnjinMinecraftPlugin.getInstance();

        if (args.length == 1) {
            StatsPlayer player = null;
            String index = args[0].toLowerCase();
            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(index);

            if (plugin.getPlayerStats().containsKey(offlinePlayer.getUniqueId().toString().toLowerCase())) {
                player = plugin.getPlayerStats().get(offlinePlayer.getUniqueId().toString());
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

    @Permission(value = "enjin.savestats")
    @Directive(parent = "enjin", value = "savestats")
    public static void saveStats(CommandSender sender, String[] args) {
        new WriteStats(EnjinMinecraftPlugin.getInstance()).write("stats.stats");
        sender.sendMessage(ChatColor.GREEN + "Stats saved to stats.stats.");
    }

    @Permission(value = "enjin.serverstats")
    @Directive(parent = "enjin", value = "serverstats")
    public static void serverStats(CommandSender sender, String[] args) {
        EnjinMinecraftPlugin plugin = EnjinMinecraftPlugin.getInstance();
        Date date = new Date(plugin.getServerStats().getLastserverstarttime());
        DateFormat format = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss z");

        sender.sendMessage(ChatColor.DARK_GREEN + "Server Stats");
        sender.sendMessage(ChatColor.DARK_GREEN + "Server Start time: " + ChatColor.GOLD + format.format(date));
        sender.sendMessage(ChatColor.DARK_GREEN + "Total number of creeper explosions: " + ChatColor.GOLD + plugin.getServerStats().getCreeperexplosions());
        sender.sendMessage(ChatColor.DARK_GREEN + "Total number of kicks: " + ChatColor.GOLD + plugin.getServerStats().getTotalkicks());
        sender.sendMessage(ChatColor.DARK_GREEN + "Kicks per player: " + ChatColor.GOLD + plugin.getServerStats().getPlayerkicks().toString());
    }
}
