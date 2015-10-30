package com.enjin.bukkit.command.commands;

import com.enjin.bukkit.EnjinMinecraftPlugin;
import com.enjin.bukkit.command.Command;
import com.enjin.bukkit.command.Directive;
import com.enjin.bukkit.command.Permission;
import com.enjin.bukkit.config.EnjinConfig;
import com.enjin.bukkit.threaded.NewKeyVerifier;
import com.enjin.bukkit.threaded.ReportPublisher;
import com.vexsoftware.votifier.Votifier;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.Plugin;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

public class CoreCommands {
    @Command(command = "enjin", aliases = "e")
    public static void enjin(CommandSender sender, String[] args) {
        // TODO: Enjin Help
    }

    @Permission(permission = "enjin.debug")
    @Directive(parent = "enjin", directive = "debug")
    public static void debug(CommandSender sender, String[] args) {
        EnjinConfig config = EnjinMinecraftPlugin.getConfiguration();
        config.setDebug(!config.isDebug());
        EnjinMinecraftPlugin.saveConfiguration();

        sender.sendMessage(ChatColor.GREEN + "Debugging has been set to " + config.isDebug());
    }

    @Permission(permission = "enjin.setkey")
    @Command(command = "enjinkey", aliases = "ek")
    @Directive(parent = "enjin", directive = "key", aliases = {"setkey", "sk", "enjinkey", "ek"})
    public static void key(CommandSender sender, String[] args) {
        if (args.length != 1) {
            return;
        }

        EnjinMinecraftPlugin.enjinlogger.info("Checking if key is valid");
        EnjinMinecraftPlugin.instance.getLogger().info("Checking if key is valid");

        NewKeyVerifier verifier = EnjinMinecraftPlugin.instance.getVerifier();
        if (verifier == null || verifier.completed) {
            EnjinMinecraftPlugin.instance.setVerifier(new NewKeyVerifier(EnjinMinecraftPlugin.instance, args[0], sender, false));
            new Thread(verifier).start();
        } else {
            sender.sendMessage(ChatColor.RED + "Please wait until we verify the key before you try again!");
        }
    }

    @Permission(permission = "enjin.push")
    @Directive(parent = "enjin", directive = "push")
    public static void push(CommandSender sender, String[] args) {
        EnjinMinecraftPlugin plugin = EnjinMinecraftPlugin.instance;

        OfflinePlayer[] players = Bukkit.getOfflinePlayers();
        Map<String, String> perms = plugin.playerperms;
        if (perms.size() > 3000 || perms.size() >= players.length) {
            int minutes = perms.size() / 3000;
            if (perms.size() % 3000 > 0) {
                minutes++;
            }

            if (perms.size() > 3000) {
                minutes += minutes * 0.1;
            }

            sender.sendMessage(ChatColor.RED + "A rank sync is still in progress, please wait until the current sync completes.");
            sender.sendMessage(ChatColor.RED + "Progress: " + Integer.toString(perms.size()) + " more player ranks to transmit, ETA: " + minutes + " minute" + (minutes > 1 ? "s" : "") + ".");
            return;
        }

        for (OfflinePlayer player : players) {
            if (player == null || player.getName() == null || player.getName().isEmpty()) {
                continue;
            }

            if (plugin.supportsUUID() && player.getUniqueId() != null) {
                perms.put(player.getName(), player.getUniqueId().toString());
            } else {
                perms.put(player.getName(), "");
            }
        }

        int minutes = perms.size() / 3000;
        if (perms.size() % 3000 > 0) {
            minutes++;
        }

        if (perms.size() > 3000) {
            minutes += minutes * 0.1;
        }

        sender.sendMessage(ChatColor.GREEN + Integer.toString(perms.size()) + " players have been queued for synchronization. This should take approximately " + Integer.toString(minutes) + " minute" + (minutes > 1 ? "s." : "."));
    }

    @Permission(permission = "enjin.report")
    @Directive(parent = "enjin", directive = "report")
    public static void report(CommandSender sender, String[] args) {
        EnjinMinecraftPlugin plugin = EnjinMinecraftPlugin.instance;
        Date date = new Date();
        DateFormat format = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss z");

        sender.sendMessage(ChatColor.GREEN + "Please wait while we generate the report");

        StringBuilder report = new StringBuilder();
        report.append("Enjin Debug Report generated on " + format.format(date) + "\n");
        report.append("Enjin plugin version: " + plugin.getDescription().getVersion() + "\n");

        Plugin permissions = null;
        if (plugin.permission != null) {
            permissions = Bukkit.getPluginManager().getPlugin(plugin.permission.getName());
        }

        if (permissions != null) {
            report.append("Permissions plugin used: " + permissions.getDescription().getName() + " version " + permissions.getDescription().getVersion() + "\n");
            report.append("Vault permissions system reported: " + plugin.permission.getName() + "\n");
        }

        if (plugin.economy != null) {
            report.append("Vault economy system reported: " + plugin.economy.getName() + "\n");
        }

        if (plugin.econcompatmode) {
            report.append("WARNING! Economy plugin doesn't support UUID, needs update.\n");
        }

        Plugin v = Bukkit.getPluginManager().getPlugin("Votifier");
        if (v != null && v.isEnabled()) {
            report.append("Votifier version: " + v.getDescription().getVersion() + "\n");
            if (v instanceof Votifier) {
                Votifier votifier = (Votifier) v;
                FileConfiguration votifierConfig = votifier.getConfig();
                String port = votifierConfig.getString("port", "");
                String host = votifierConfig.getString("host", "");
                report.append("Votifier is enabled properly: " + !(votifier.getVoteReceiver() == null) + "\n");
                report.append("Votifier is listening on: " + host + ":" + port + "\n");
            }
        }

        report.append("Bukkit version: " + Bukkit.getVersion() + "\n");
        report.append("Java version: " + System.getProperty("java.version") + " " + System.getProperty("java.vendor") + "\n");
        report.append("Operating system: " + System.getProperty("os.name") + " " + System.getProperty("os.version") + " " + System.getProperty("os.arch") + "\n");

        if (plugin.authkeyinvalid) {
            report.append("ERROR: Authkey reported by plugin as invalid!\n");
        }

        if (plugin.unabletocontactenjin) {
            report.append("WARNING: Plugin has been unable to contact Enjin for the past 5 minutes\n");
        }

        if (plugin.permissionsnotworking) {
            report.append("WARNING: Permissions plugin is not configured properly and is disabled. Check the server.log for more details.\n");
        }

        report.append("\nPlugins: \n");
        for (Plugin p : Bukkit.getPluginManager().getPlugins()) {
            report.append(p.getName() + " version " + p.getDescription().getVersion() + "\n");
        }

        report.append("\nWorlds: \n");
        for (World world : Bukkit.getWorlds()) {
            report.append(world.getName() + "\n");
        }

        Bukkit.getScheduler().runTaskAsynchronously(plugin, new ReportPublisher(plugin, report, sender));
    }
}
