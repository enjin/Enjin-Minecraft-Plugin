package com.enjin.sponge.command.commands;

import com.enjin.sponge.EnjinMinecraftPlugin;
import com.enjin.sponge.command.Directive;
import com.enjin.sponge.command.Permission;
import com.enjin.sponge.managers.StatsManager;
import com.enjin.sponge.stats.StatsPlayer;
import com.enjin.sponge.stats.WriteStats;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColor;
import org.spongepowered.api.text.format.TextColors;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class StatCommands {
    @Permission("enjin.customstat")
    @Directive(parent = "enjin", value = "customstat")
    public static void customStat(CommandSource sender, String[] args) {
        if (args.length == 5) {
            String player = args[0].trim();
            String plugin = args[1].trim();
            String statName = args[2].trim();
            String statValue = args[3].trim();
            String cumulative = args[4].trim();
            boolean existing = cumulative.equalsIgnoreCase("true");
            Player offlinePlayer = Sponge.getServer().getPlayer(player).get();

            if (offlinePlayer == null) {
                return;
            }

            StatsPlayer statsPlayer = StatsManager.getPlayerStats(offlinePlayer);

            try {
                statsPlayer.addCustomStat(plugin, statName, statValue.indexOf(".") > -1 ? Double.parseDouble(statValue) : Integer.parseInt(statValue), existing);
                sender.sendMessage(Text.of(TextColors.GREEN, "Successfully set the custom value!"));
            } catch (NumberFormatException e) {
                sender.sendMessage(Text.of(TextColors.RED, "I'm sorry, custom values can only be numerical."));
            }
        } else {
            sender.sendMessage(Text.of(TextColors.DARK_RED, "Usage: /enjin customstat <player> <plugin> <stat-name> <stat-value> <cumulative>"));
        }
    }

    @Permission(value = "enjin.playerstats")
    @Directive(parent = "enjin", value = "playerstats")
    public static void playerStats(CommandSource sender, String[] args) {
        EnjinMinecraftPlugin plugin = EnjinMinecraftPlugin.getInstance();

        if (args.length == 1) {
            StatsPlayer player = null;
            String index = args[0].toLowerCase();
            Player offlinePlayer = Sponge.getServer().getPlayer(index).get();

            if (offlinePlayer == null) {
                return;
            }

            if (plugin.getPlayerStats().containsKey(offlinePlayer.getUniqueId().toString().toLowerCase())) {
                player = plugin.getPlayerStats().get(offlinePlayer.getUniqueId().toString());
            }

            if (player != null) {
                sender.sendMessage(Text.of(TextColors.DARK_GREEN, "Player stats for player: ", TextColors.GOLD, player.getName()));
                sender.sendMessage(Text.of(TextColors.DARK_GREEN, "Deaths: ", TextColors.GOLD, player.getDeaths()));
                sender.sendMessage(Text.of(TextColors.DARK_GREEN, "Kills: ", TextColors.GOLD, player.getKilled()));
                sender.sendMessage(Text.of(TextColors.DARK_GREEN, "Blocks broken: ", TextColors.GOLD, player.getBrokenblocks()));
                sender.sendMessage(Text.of(TextColors.DARK_GREEN, "Blocks placed: ", TextColors.GOLD, player.getPlacedblocks()));
                sender.sendMessage(Text.of(TextColors.DARK_GREEN, "Block types broken: ", TextColors.GOLD, player.getBrokenblocktypes().toString()));
                sender.sendMessage(Text.of(TextColors.DARK_GREEN, "Block types placed: ", TextColors.GOLD, player.getPlacedblocktypes().toString()));
                sender.sendMessage(Text.of(TextColors.DARK_GREEN, "Foot distance traveled: ", TextColors.GOLD, player.getFootdistance()));
                sender.sendMessage(Text.of(TextColors.DARK_GREEN, "Boat distance traveled: ", TextColors.GOLD, player.getBoatdistance()));
                sender.sendMessage(Text.of(TextColors.DARK_GREEN, "Minecart distance traveled: ", TextColors.GOLD, player.getMinecartdistance()));
                sender.sendMessage(Text.of(TextColors.DARK_GREEN, "Pig distance traveled: ", TextColors.GOLD, player.getPigdistance()));
            } else {
                sender.sendMessage(Text.of("I'm sorry, but I couldn't find a player with stats with that name."));
            }
        } else {
            sender.sendMessage(Text.of("USAGE: /enjin playerstats <player>"));
        }
    }

    @Permission(value = "enjin.savestats")
    @Directive(parent = "enjin", value = "savestats")
    public static void saveStats(CommandSource sender, String[] args) {
        new WriteStats().write(StatsManager.getStatFile());
        sender.sendMessage(Text.of(TextColors.GREEN, "Stats saved to enjin-stats.json."));
    }

    @Permission(value = "enjin.serverstats")
    @Directive(parent = "enjin", value = "serverstats")
    public static void serverStats(CommandSource sender, String[] args) {
        EnjinMinecraftPlugin plugin = EnjinMinecraftPlugin.getInstance();
        Date date = new Date(plugin.getServerStats().getLastserverstarttime());
        DateFormat format = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss z");

        sender.sendMessage(Text.of(TextColors.DARK_GREEN, "Server Stats"));
        sender.sendMessage(Text.of(TextColors.DARK_GREEN, "Server Start time: ", TextColors.GOLD, format.format(date)));
        sender.sendMessage(Text.of(TextColors.DARK_GREEN, "Total number of creeper explosions: ", TextColors.GOLD, plugin.getServerStats().getCreeperexplosions()));
        sender.sendMessage(Text.of(TextColors.DARK_GREEN, "Total number of kicks: ", TextColors.GOLD, plugin.getServerStats().getTotalkicks()));
        sender.sendMessage(Text.of(TextColors.DARK_GREEN, "Kicks per player: ", TextColors.GOLD, plugin.getServerStats().getPlayerkicks().toString()));
    }
}
