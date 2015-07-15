package com.enjin.emp.bungee;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;

import com.enjin.officialplugin.points.EnjinPointsSyncClass;
import com.enjin.officialplugin.points.PointsAPI;
import com.enjin.officialplugin.points.RetrievePointsSyncClass;

import net.md_5.bungee.BungeeCord;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.plugin.Command;
import net.md_5.bungee.api.plugin.Plugin;

public class EnjinCommand extends Command {

    EnjinPlugin plugin;

    public EnjinCommand(EnjinPlugin plugin) {
        super("benjin");
        this.plugin = plugin;
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (args.length > 0) {
            if (args[0].equalsIgnoreCase("key")) {
                if (!sender.hasPermission("enjin.setkey")) {
                    TextComponent message = new TextComponent("You need to have the \"enjin.setkey\" permission or OP to run that command!");
                    message.setColor(ChatColor.RED);
                    sender.sendMessage(message);
                    return;
                }
                if (args.length != 2) {
                    return;
                }
                EnjinPlugin.enjinlogger.info("Checking if key is valid");
                plugin.logger.info("Checking if key is valid");
                //Make sure we don't have several verifier threads going at the same time.
                if (plugin.verifier == null || plugin.verifier.completed) {
                    plugin.verifier = new NewKeyVerifier(plugin, args[1], sender, false);
                    BungeeCord.getInstance().getScheduler().runAsync(plugin, plugin.verifier);
                } else {
                    TextComponent message = new TextComponent("Please wait until we verify the key before you try again!");
                    message.setColor(ChatColor.RED);
                    sender.sendMessage(message);
                }
                return;
            } else if (args[0].equalsIgnoreCase("report")) {
                if (!sender.hasPermission("enjin.report")) {
                    TextComponent message = new TextComponent("You need to have the \"enjin.report\" permission or OP to run that command!");
                    message.setColor(ChatColor.RED);
                    sender.sendMessage(message);
                    return;
                }
                TextComponent message = new TextComponent("Please wait as we generate the report");
                message.setColor(ChatColor.GREEN);
                sender.sendMessage(message);
                DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss z");
                Date date = new Date();
                StringBuilder report = new StringBuilder();
                report.append("Enjin Debug Report generated on " + dateFormat.format(date) + "\n");
                report.append("Enjin plugin version: " + plugin.getDescription().getVersion() + "\n");
                report.append("Bungee version: " + plugin.getProxy().getVersion() + "\n");
                report.append("Java version: " + System.getProperty("java.version") + " " + System.getProperty("java.vendor") + "\n");
                report.append("Operating system: " + System.getProperty("os.name") + " " + System.getProperty("os.version") + " " + System.getProperty("os.arch") + "\n");

                if (plugin.authkeyinvalid) {
                    report.append("ERROR: Authkey reported by plugin as invalid!\n");
                }
                if (plugin.unabletocontactenjin) {
                    report.append("WARNING: Plugin has been unable to contact Enjin for the past 5 minutes\n");
                }

                report.append("\nPlugins: \n");
                Collection<Plugin> plugins = BungeeCord.getInstance().getPluginManager().getPlugins();
                for (Plugin p : plugins) {
                    report.append(p.getDescription().getName() + " version " + p.getDescription().getVersion() + "\n");
                }
                ReportMakerThread rmthread = new ReportMakerThread(plugin, report, sender);
                BungeeCord.getInstance().getScheduler().runAsync(plugin, rmthread);
                return;
            } else if (args[0].equalsIgnoreCase("debug")) {
                if (!sender.hasPermission("enjin.debug")) {
                    TextComponent message = new TextComponent("You need to have the \"enjin.debug\" permission or OP to run that command!");
                    message.setColor(ChatColor.RED);
                    sender.sendMessage(message);
                    return;
                }
                if (EnjinPlugin.debug) {
                    EnjinPlugin.debug = false;
                } else {
                    EnjinPlugin.debug = true;
                }
                TextComponent message = new TextComponent("Debugging has been set to " + EnjinPlugin.debug);
                message.setColor(ChatColor.GREEN);
                sender.sendMessage(message);
                return;
            } else if (args[0].equalsIgnoreCase("addpoints")) {
                if (!sender.hasPermission("enjin.points.add")) {
                    TextComponent message = new TextComponent("You need to have the \"enjin.points.add\" permission or OP to run that command!");
                    message.setColor(ChatColor.RED);
                    sender.sendMessage(message);
                    return;
                }
                if (args.length > 2) {
                    String playername = args[1].trim();
                    int pointsamount = 1;
                    try {
                        pointsamount = Integer.parseInt(args[2].trim());
                    } catch (NumberFormatException e) {
                        TextComponent message = new TextComponent("Usage: /benjin addpoints [player] [amount]");
                        message.setColor(ChatColor.DARK_RED);
                        sender.sendMessage(message);
                        return;
                    }
                    if (pointsamount < 1) {
                        TextComponent message = new TextComponent("You cannot add less than 1 point to a user. You might want to try /enjin removepoints!");
                        message.setColor(ChatColor.DARK_RED);
                        sender.sendMessage(message);
                        return;
                    }
                    TextComponent message = new TextComponent("Please wait as we add the " + pointsamount + " points to " + playername + "...");
                    message.setColor(ChatColor.GOLD);
                    sender.sendMessage(message);
                    EnjinPointsSyncClass mthread = new EnjinPointsSyncClass(sender, playername, pointsamount, PointsAPI.Type.AddPoints);
                    BungeeCord.getInstance().getScheduler().runAsync(plugin, mthread);
                } else {
                    TextComponent message = new TextComponent("Usage: /benjin addpoints [player] [amount]");
                    message.setColor(ChatColor.DARK_RED);
                    sender.sendMessage(message);
                }
                return;
            } else if (args[0].equalsIgnoreCase("removepoints")) {
                if (!sender.hasPermission("enjin.points.remove")) {
                    TextComponent message = new TextComponent("You need to have the \"enjin.points.remove\" permission or OP to run that command!");
                    message.setColor(ChatColor.RED);
                    sender.sendMessage(message);
                    return;
                }
                if (args.length > 2) {
                    String playername = args[1].trim();
                    int pointsamount = 1;
                    try {
                        pointsamount = Integer.parseInt(args[2].trim());
                    } catch (NumberFormatException e) {
                        TextComponent message = new TextComponent("Usage: /benjin removepoints [player] [amount]");
                        message.setColor(ChatColor.DARK_RED);
                        sender.sendMessage(message);
                        return;
                    }
                    if (pointsamount < 1) {
                        TextComponent message = new TextComponent("You cannot remove less than 1 point to a user.");
                        message.setColor(ChatColor.DARK_RED);
                        sender.sendMessage(message);
                        return;
                    }
                    TextComponent message = new TextComponent("Please wait as we remove the " + pointsamount + " points from " + playername + "...");
                    message.setColor(ChatColor.GOLD);
                    sender.sendMessage(message);
                    EnjinPointsSyncClass mthread = new EnjinPointsSyncClass(sender, playername, pointsamount, PointsAPI.Type.RemovePoints);
                    BungeeCord.getInstance().getScheduler().runAsync(plugin, mthread);
                } else {
                    TextComponent message = new TextComponent("Usage: /benjin removepoints [player] [amount]");
                    message.setColor(ChatColor.DARK_RED);
                    sender.sendMessage(message);
                }
                return;
            } else if (args[0].equalsIgnoreCase("setpoints")) {
                if (!sender.hasPermission("enjin.points.set")) {
                    TextComponent message = new TextComponent("You need to have the \"enjin.points.set\" permission or OP to run that command!");
                    message.setColor(ChatColor.RED);
                    sender.sendMessage(message);
                    return;
                }
                if (args.length > 2) {
                    String playername = args[1].trim();
                    int pointsamount = 1;
                    try {
                        pointsamount = Integer.parseInt(args[2].trim());
                    } catch (NumberFormatException e) {
                        TextComponent message = new TextComponent("Usage: /enjin setpoints [player] [amount]");
                        message.setColor(ChatColor.DARK_RED);
                        sender.sendMessage(message);
                        return;
                    }
                    TextComponent message = new TextComponent("Please wait as we set the points to " + pointsamount + " points for " + playername + "...");
                    message.setColor(ChatColor.GOLD);
                    sender.sendMessage(message);
                    EnjinPointsSyncClass mthread = new EnjinPointsSyncClass(sender, playername, pointsamount, PointsAPI.Type.SetPoints);
                    BungeeCord.getInstance().getScheduler().runAsync(plugin, mthread);
                } else {
                    TextComponent message = new TextComponent("Usage: /benjin setpoints [player] [amount]");
                    message.setColor(ChatColor.DARK_RED);
                    sender.sendMessage(message);
                }
                return;
            } else if (args[0].equalsIgnoreCase("points")) {
                if (args.length > 1 && sender.hasPermission("enjin.points.getothers")) {
                    String playername = args[1].trim();
                    TextComponent message = new TextComponent("Please wait as we retrieve the points balance for " + playername + "...");
                    message.setColor(ChatColor.GOLD);
                    sender.sendMessage(message);
                    RetrievePointsSyncClass mthread = new RetrievePointsSyncClass(sender, playername, false);
                    BungeeCord.getInstance().getScheduler().runAsync(plugin, mthread);
                } else if (sender.hasPermission("enjin.points.getself")) {
                    TextComponent message = new TextComponent("Please wait as we retrieve your points balance...");
                    message.setColor(ChatColor.GOLD);
                    sender.sendMessage(message);
                    RetrievePointsSyncClass mthread = new RetrievePointsSyncClass(sender, sender.getName(), true);
                    BungeeCord.getInstance().getScheduler().runAsync(plugin, mthread);
                } else {
                    TextComponent message = new TextComponent("I'm sorry, you don't have permission to check points!");
                    message.setColor(ChatColor.DARK_RED);
                    sender.sendMessage(message);
                }
                return;
            } else if (args[0].equalsIgnoreCase("tags")) {
                if (!sender.hasPermission("enjin.tags.view")) {
                    TextComponent message = new TextComponent("You need to have the \"enjin.tags.view\" permission or OP to run that command!");
                    message.setColor(ChatColor.RED);
                    sender.sendMessage(message);
                    return;
                }
                if (args.length > 1) {
                    String playername = args[1].trim();
                    TextComponent message = new TextComponent("Please wait as we retrieve the tags for " + playername + "...");
                    message.setColor(ChatColor.GOLD);
                    sender.sendMessage(message);
                    EnjinRetrievePlayerTags mthread = new EnjinRetrievePlayerTags(playername, sender, plugin);
                    BungeeCord.getInstance().getScheduler().runAsync(plugin, mthread);
                } else {
                    TextComponent message = new TextComponent("Usage: /benjin tags <player>");
                    message.setColor(ChatColor.DARK_RED);
                    sender.sendMessage(message);
                }
                return;
            }
        } else {
            /*
             * Display detailed Enjin help in console
			 */
            sender.sendMessages(EnjinConsole.header());

            if (sender.hasPermission("enjin.setkey"))
                sender.sendMessage(ChatColor.GOLD + "/enjin key <KEY>: "
                        + ChatColor.RESET + "Enter the secret key from your " + ChatColor.GRAY + "Admin - Games - Minecraft - Enjin Plugin " + ChatColor.RESET + "page.");
            if (sender.hasPermission("enjin.debug"))
                sender.sendMessage(ChatColor.GOLD + "/enjin debug: "
                        + ChatColor.RESET + "Enable debug mode and display extra information in console.");
            if (sender.hasPermission("enjin.report"))
                sender.sendMessage(ChatColor.GOLD + "/enjin report: "
                        + ChatColor.RESET + "Generate a report file that you can send to Enjin Support for troubleshooting.");
            if (sender.hasPermission("enjin.tags.view"))
                sender.sendMessage(ChatColor.GOLD + "/enjin tags <player>: "
                        + ChatColor.RESET + "Shows the tags on the website for the player.");

            // Points commands
            if (sender.hasPermission("enjin.points.getself"))
                sender.sendMessage(ChatColor.GOLD + "/enjin points: "
                        + ChatColor.RESET + "Shows your current website points.");
            if (sender.hasPermission("enjin.points.getothers"))
                sender.sendMessage(ChatColor.GOLD + "/enjin points <NAME>: "
                        + ChatColor.RESET + "Shows another player's current website points.");
            if (sender.hasPermission("enjin.points.add"))
                sender.sendMessage(ChatColor.GOLD + "/enjin addpoints <NAME> <AMOUNT>: "
                        + ChatColor.RESET + "Add points to a player.");
            if (sender.hasPermission("enjin.points.remove"))
                sender.sendMessage(ChatColor.GOLD + "/enjin removepoints <NAME> <AMOUNT>: "
                        + ChatColor.RESET + "Remove points from a player.");
            if (sender.hasPermission("enjin.points.set"))
                sender.sendMessage(ChatColor.GOLD + "/enjin setpoints <NAME> <AMOUNT>: "
                        + ChatColor.RESET + "Set a player's total points.");
        }
    }

}
