package com.enjin.bungee.command.commands;

import com.enjin.bungee.EnjinMinecraftPlugin;
import com.enjin.bungee.command.Command;
import com.enjin.bungee.command.Directive;
import com.enjin.bungee.command.Permission;
import com.enjin.bungee.tasks.ReportPublisher;
import com.enjin.bungee.util.io.EnjinConsole;
import com.enjin.core.Enjin;
import com.enjin.core.EnjinServices;
import com.enjin.core.config.EnjinConfig;
import com.enjin.rpc.mappings.mappings.general.RPCData;
import com.enjin.rpc.mappings.mappings.plugin.TagData;
import com.enjin.rpc.mappings.services.PluginService;
import com.google.common.base.Optional;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.plugin.Plugin;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

public class CoreCommands {
    @Command(value = "benjin", aliases = "be", requireValidKey = false)
    public static void enjin(CommandSender sender, String[] args) {
        for (String line : EnjinConsole.header()) {
            sender.sendMessage(line);
        }

        if (sender.hasPermission("enjin.setkey")) {
            sender.sendMessage(ChatColor.GOLD + "/benjin key <KEY>: " + ChatColor.RESET + "Enter the secret key from your " + ChatColor.GRAY + "Admin - Games - Minecraft - Enjin Plugin " + ChatColor.RESET + "page.");
        }

        if (sender.hasPermission("enjin.debug")) {
            sender.sendMessage(ChatColor.GOLD + "/benjin debug: " + ChatColor.RESET + "Enable debug mode and display extra information in console.");
        }

        if (sender.hasPermission("enjin.report")) {
            sender.sendMessage(ChatColor.GOLD + "/benjin report: " + ChatColor.RESET + "Generate a report file that you can send to Enjin Support for troubleshooting.");
        }

        if (sender.hasPermission("enjin.tags.view")) {
            sender.sendMessage(ChatColor.GOLD + "/benjin tags <player>: " + ChatColor.RESET + "Shows the tags on the website for the player.");
        }

        // Points commands
        if (sender.hasPermission("enjin.points.getself")) {
            sender.sendMessage(ChatColor.GOLD + "/benjin points: " + ChatColor.RESET + "Shows your current website points.");
        }

        if (sender.hasPermission("enjin.points.getothers")) {
            sender.sendMessage(ChatColor.GOLD + "/benjin points <NAME>: " + ChatColor.RESET + "Shows another player's current website points.");
        }

        if (sender.hasPermission("enjin.points.add")) {
            sender.sendMessage(ChatColor.GOLD + "/benjin addpoints <NAME> <AMOUNT>: " + ChatColor.RESET + "Add points to a player.");
        }

        if (sender.hasPermission("enjin.points.remove")) {
            sender.sendMessage(ChatColor.GOLD + "/benjin removepoints <NAME> <AMOUNT>: " + ChatColor.RESET + "Remove points from a player.");
        }

        if (sender.hasPermission("enjin.points.set")) {
            sender.sendMessage(ChatColor.GOLD + "/benjin setpoints <NAME> <AMOUNT>: " + ChatColor.RESET + "Set a player's total points.");
        }
    }

    @Permission(value = "enjin.debug")
    @Directive(parent = "benjin", value = "debug", requireValidKey = false)
    public static void debug(CommandSender sender, String[] args) {
        EnjinConfig config = Enjin.getConfiguration();
        config.setDebug(!config.isDebug());
        EnjinMinecraftPlugin.saveConfiguration();
        sender.sendMessage(ChatColor.GREEN + "Debugging has been set to " + config.isDebug());
    }

    @Permission(value = "enjin.setkey")
    @Command(value = "benjinkey", aliases = "bek", requireValidKey = false)
    @Directive(parent = "benjin", value = "key", aliases = {"setkey", "sk", "enjinkey", "ek"}, requireValidKey = false)
    public static void key(final CommandSender sender, final String[] args) {
        if (args.length != 1) {
            sender.sendMessage("USAGE: /enjin key <key>");
            return;
        }

        Enjin.getLogger().info("Checking if key is valid");
        EnjinMinecraftPlugin.getInstance().getLogger().info("Checking if key is valid");

        ProxyServer.getInstance().getScheduler().runAsync(EnjinMinecraftPlugin.getInstance(), new Runnable() {
            @Override
            public void run() {
                if (Enjin.getConfiguration().getAuthKey().equals(args[0])) {
                    sender.sendMessage(ChatColor.GREEN + "That key has already been validated.");
                    return;
                }

                Optional<Integer> port = EnjinMinecraftPlugin.getPort();
                PluginService service = EnjinServices.getService(PluginService.class);
                RPCData<Boolean> data = service.auth(Optional.of(args[0]), port.isPresent() ? port.get() : null, true);

                if (data == null) {
                    sender.sendMessage("A fatal error has occurred. Please try again later. If the problem persists please contact Enjin support.");
                    return;
                }

                if (data.getError() != null) {
                    sender.sendMessage(ChatColor.RED + data.getError().getMessage());
                    return;
                }

                if (data.getResult().booleanValue()) {
                    sender.sendMessage(ChatColor.GREEN + "The key has been successfully validated.");
                    Enjin.getConfiguration().setAuthKey(args[0]);
                    EnjinMinecraftPlugin.saveConfiguration();

                    if (EnjinMinecraftPlugin.getInstance().isAuthKeyInvalid()) {
                        EnjinMinecraftPlugin.getInstance().setAuthKeyInvalid(false);
                        EnjinMinecraftPlugin.getInstance().init();
                    }
                } else {
                    sender.sendMessage(ChatColor.RED + "We were unable to validate the provided key.");
                }
            }
        });
    }

    @Permission(value = "enjin.report")
    @Directive(parent = "benjin", value = "report", requireValidKey = false)
    public static void report(CommandSender sender, String[] args) {
        EnjinMinecraftPlugin plugin = EnjinMinecraftPlugin.getInstance();
        Date date = new Date();
        DateFormat format = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss z");

        sender.sendMessage(ChatColor.GREEN + "Please wait while we generate the report");

        StringBuilder report = new StringBuilder();
        report.append("Enjin Debug Report generated on " + format.format(date) + "\n");
        report.append("Enjin plugin version: " + plugin.getDescription().getVersion() + "\n");

        report.append("BungeeCord version: " + ProxyServer.getInstance().getVersion() + "\n");
        report.append("Java version: " + System.getProperty("java.version") + " " + System.getProperty("java.vendor") + "\n");
        report.append("Operating system: " + System.getProperty("os.name") + " " + System.getProperty("os.version") + " " + System.getProperty("os.arch") + "\n");

        if (plugin.isAuthKeyInvalid()) {
            report.append("ERROR: Authkey reported by plugin as invalid!\n");
        }

        if (plugin.isUnableToContactEnjin()) {
            report.append("WARNING: Plugin has been unable to contact Enjin for the past 5 minutes\n");
        }

        report.append("\nPlugins: \n");
        for (Plugin p : ProxyServer.getInstance().getPluginManager().getPlugins()) {
            report.append(p.getDescription().getName() + " version " + p.getDescription().getVersion() + "\n");
        }

        ProxyServer.getInstance().getScheduler().runAsync(plugin, new ReportPublisher(plugin, report, sender));
    }

    @Permission(value = "enjin.tags")
    @Directive(parent = "benjin", value = "tags", requireValidKey = true)
    public static void tags(CommandSender sender, String[] args) {
        if (args.length == 0) {
            sender.sendMessage("/enjin tags <player>");
            return;
        }

        String name = args[0].substring(0, args[0].length() > 16 ? 16 : args[0].length());
        PluginService service = EnjinServices.getService(PluginService.class);
        RPCData<List<TagData>> data = service.getTags(name);

        if (data == null) {
            sender.sendMessage("A fatal error has occurred. Please try again later. If the problem persists please contact Enjin support.");
            return;
        }

        if (data.getError() != null) {
            sender.sendMessage(data.getError().getMessage());
            return;
        }

        List<TagData> tags = data.getResult();
        String tagList = "";
        if (tags != null) {
            Iterator<TagData> iterator = tags.iterator();
            while (iterator.hasNext()) {
                if (!tagList.isEmpty()) {
                    tagList += ChatColor.GOLD + ", ";
                }

                TagData tag = iterator.next();
                tagList += ChatColor.GREEN + tag.getName();
            }
        }

        sender.sendMessage(ChatColor.GOLD + name + "'s Tags: " + tagList);
    }
}
