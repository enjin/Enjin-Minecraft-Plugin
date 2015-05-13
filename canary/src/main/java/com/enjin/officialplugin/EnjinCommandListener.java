package com.enjin.officialplugin;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import com.enjin.officialplugin.threaded.NewKeyVerifier;
import com.enjin.officialplugin.threaded.ReportMakerThread;

import net.canarymod.Canary;
import net.canarymod.api.OfflinePlayer;
import net.canarymod.api.entity.living.humanoid.Player;
import net.canarymod.api.world.World;
import net.canarymod.chat.Colors;
import net.canarymod.chat.MessageReceiver;
import net.canarymod.chat.TextFormat;
import net.canarymod.commandsys.Command;
import net.canarymod.commandsys.CommandListener;
import net.canarymod.plugin.Plugin;

public class EnjinCommandListener implements CommandListener {

    EnjinMinecraftPlugin plugin;

    public EnjinCommandListener(EnjinMinecraftPlugin plugin) {
        this.plugin = plugin;
    }

    @Command(aliases = {"enjinkey", "ek"},
            description = "Sets the enjin key.",
            permissions = {"enjin.setkey"},
            toolTip = "/enjinkey <key>",
            min = 2)
    public void setEnjinKey(MessageReceiver caller, String[] args) {
        EnjinMinecraftPlugin.enjinlogger.info("Checking if key is valid");
        EnjinMinecraftPlugin.logger.info("Checking if key is valid");
        //Make sure we don't have several verifier threads going at the same time.
        if (plugin.verifier == null || plugin.verifier.completed) {
            plugin.verifier = new NewKeyVerifier(plugin, args[1], caller, false);
            Thread verifierthread = new Thread(plugin.verifier);
            verifierthread.start();
        } else {
            caller.message(Colors.RED + "Please wait until we verify the key before you try again!");
        }
    }

    @Command(aliases = {"enjin", "e"},
            description = "The main enjin plugin command",
            permissions = {},
            toolTip = "/enjin <key|report|debug|push|broadcast|lag>",
            min = 1)
    public void mainEnjinCommand(MessageReceiver sender, String[] args) {

        /*
         * Display detailed Enjin help in console
         */
        for (String s : EnjinConsole.header()) {
            sender.message(s);
        }

        if (sender.hasPermission("enjin.setkey"))
            sender.message(Colors.ORANGE + "/enjin key <KEY>: "
                    + TextFormat.RESET + "Enter the secret key from your " + Colors.GRAY + "Admin - Games - Minecraft - Enjin Plugin " + TextFormat.RESET + "page.");
        if (sender.hasPermission("enjin.broadcast"))
            sender.message(Colors.ORANGE + "/enjin broadcast <MESSAGE>: "
                    + TextFormat.RESET + "Broadcast a message to all players.");
        if (sender.hasPermission("enjin.push"))
            sender.message(Colors.ORANGE + "/enjin push: "
                    + TextFormat.RESET + "Sync your website tags with the current ranks.");
        if (sender.hasPermission("enjin.lag"))
            sender.message(Colors.ORANGE + "/enjin lag: "
                    + TextFormat.RESET + "Display TPS average and memory usage.");
        if (sender.hasPermission("enjin.debug"))
            sender.message(Colors.ORANGE + "/enjin debug: "
                    + TextFormat.RESET + "Enable debug mode and display extra information in console.");
        if (sender.hasPermission("enjin.report"))
            sender.message(Colors.ORANGE + "/enjin report: "
                    + TextFormat.RESET + "Generate a report file that you can send to Enjin Support for troubleshooting.");

        /*
        // Shop buy commands
        sender.message(Colors.GOLD + "/buy: "
                + Colors.RESET + "Display items available for purchase.");
        sender.message(Colors.GOLD + "/buy page <#>: "
                + Colors.RESET + "View the next page of results.");
        sender.message(Colors.GOLD + "/buy <ID>: "
                + Colors.RESET + "Purchase the specified item ID in the server shop.");
        */
    }

    @Command(aliases = {"key"},
            parent = "enjin",
            helpLookup = "enjin key",
            description = "Sets the enjin key.",
            permissions = {"enjin.setkey"},
            toolTip = "/enjin key <key>",
            min = 2)
    public void subSetKeyCommand(MessageReceiver sender, String[] args) {
        setEnjinKey(sender, args);
    }

    @Command(aliases = {"report"},
            parent = "enjin",
            helpLookup = "enjin report",
            description = "Generates the enjin report for submitting to enjin.",
            permissions = {"enjin.report"},
            toolTip = "/enjin report",
            min = 1)
    public void getEnjinReport(MessageReceiver sender, String[] args) {
        sender.message(Colors.GREEN + "Please wait as we generate the report");
        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss z");
        Date date = new Date();
        StringBuilder report = new StringBuilder();
        report.append("Enjin Debug Report generated on " + dateFormat.format(date) + "\n");
        report.append("Enjin plugin version: " + plugin.getVersion() + "\n");
        if (plugin.votifierinstalled) {
            /*
			String votiferversion = Bukkit.getPluginManager().getPlugin("Votifier").getDescription().getVersion();
			report.append("Votifier version: " + votiferversion + "\n");*/
        }
        //report.append("Canarymod version: " + getServer().getVersion() + "\n");
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
        for (String p : Canary.loader().getPluginList()) {
            Plugin pl = Canary.loader().getPlugin(p);
            report.append(pl.getName() + " version " + pl.getVersion() + "\n");
        }
        report.append("\nWorlds: \n");
        for (World world : Canary.getServer().getWorldManager().getAllWorlds()) {
            report.append(world.getName() + "\n");
        }
        ReportMakerThread rmthread = new ReportMakerThread(plugin, report, sender);
        Thread dispatchThread = new Thread(rmthread);
        dispatchThread.start();
    }

    @Command(aliases = {"debug"},
            parent = "enjin",
            helpLookup = "enjin debug",
            description = "Toggles debug info in console.",
            permissions = {"enjin.debug"},
            toolTip = "/enjin debug",
            min = 1)
    public void toggleDebugMode(MessageReceiver sender, String[] args) {
        if (EnjinMinecraftPlugin.debug) {
            EnjinMinecraftPlugin.debug = false;
        } else {
            EnjinMinecraftPlugin.debug = true;
        }
        sender.message(Colors.GREEN + "Debugging has been set to " + EnjinMinecraftPlugin.debug);
    }

    @Command(aliases = {"push"},
            parent = "enjin",
            helpLookup = "enjin push",
            description = "Sends all the player's groups to enjin.",
            permissions = {"enjin.push"},
            toolTip = "/enjin push",
            min = 1)
    public void pushPlayerGroups(MessageReceiver sender, String[] args) {
        ArrayList<OfflinePlayer> allplayers = EnjinMinecraftPlugin.getAllPlayersData();
        if (plugin.playerperms.size() > 3000 || plugin.playerperms.size() >= allplayers.size()) {
            int minutes = plugin.playerperms.size() / 3000;
            //Make sure to tack on an extra minute for the leftover players.
            if (plugin.playerperms.size() % 3000 > 0) {
                minutes++;
            }
            //Add an extra 10% if it's going to take more than one synch.
            //Just in case a synch fails.
            if (plugin.playerperms.size() > 3000) {
                minutes += minutes * 0.1;
            }
            sender.message(Colors.RED + "A rank sync is still in progress, please wait until the current sync completes.");
            sender.message(Colors.RED + "Progress:" + Integer.toString(plugin.playerperms.size()) + " more player ranks to transmit, ETA: " + minutes + " minute" + (minutes > 1 ? "s" : "") + ".");
            return;
        }
        for (OfflinePlayer offlineplayer : allplayers) {
            plugin.playerperms.put(offlineplayer.getName(), "");
        }

        //Calculate how many minutes approximately it's going to take.
        int minutes = plugin.playerperms.size() / 3000;
        //Make sure to tack on an extra minute for the leftover players.
        if (plugin.playerperms.size() % 3000 > 0) {
            minutes++;
        }
        //Add an extra 10% if it's going to take more than one synch.
        //Just in case a synch fails.
        if (plugin.playerperms.size() > 3000) {
            minutes += minutes * 0.1;
        }
        if (minutes == 1) {
            sender.message(Colors.GREEN + Integer.toString(plugin.playerperms.size()) + " players have been queued for synching. This should take approximately " + Integer.toString(minutes) + " minute.");
        } else {
            sender.message(Colors.GREEN + Integer.toString(plugin.playerperms.size()) + " players have been queued for synching. This should take approximately " + Integer.toString(minutes) + " minutes.");
        }
        return;
    }

    @Command(aliases = {"inform"},
            parent = "enjin",
            helpLookup = "enjin inform",
            description = "Sends a notice to the user. Supports color codes.",
            permissions = {"enjin.inform"},
            toolTip = "/enjin inform <message>",
            min = 2)
    public void informPlayer(MessageReceiver sender, String[] args) {
        Player player = Canary.getServer().getPlayer(args[1]);
        if (player == null) {
            sender.message(Colors.RED + "That player isn't on the server at the moment.");
            return;
        }
        StringBuilder thestring = new StringBuilder();
        for (int i = 2; i < args.length; i++) {
            if (i > 2) {
                thestring.append(" ");
            }
            thestring.append(args[i]);
        }
        player.sendMessage(EnjinConsole.translateColorCodes(thestring.toString()));
    }

    @Command(aliases = {"broadcast"},
            parent = "enjin",
            helpLookup = "enjin broadcast",
            description = "Broadcasts the message to the entire server. Supports colorcodes.",
            permissions = {"enjin.broadcast"},
            toolTip = "/enjin broadcast <message>",
            min = 2)
    public void broadcastMessage(MessageReceiver sender, String[] args) {
        StringBuilder thestring = new StringBuilder();
        for (int i = 1; i < args.length; i++) {
            if (i > 1) {
                thestring.append(" ");
            }
            thestring.append(args[i]);
        }
        Canary.getServer().broadcastMessage(EnjinConsole.translateColorCodes(thestring.toString()));
    }

    @Command(aliases = {"lag"},
            parent = "enjin",
            helpLookup = "enjin lag",
            description = "Shows the current and average TPS for the server.",
            permissions = {"enjin.lag"},
            toolTip = "/enjin lag",
            min = 1)
    public void getLagMeasurement(MessageReceiver sender, String[] args) {
        sender.message(Colors.ORANGE + "Average TPS: " + Colors.GREEN + plugin.tpstask.getTPSAverage());
        sender.message(Colors.ORANGE + "Last TPS measurement: " + Colors.GREEN + plugin.tpstask.getLastTPSMeasurement());
        Runtime runtime = Runtime.getRuntime();
        long memused = runtime.totalMemory() / (1024 * 1024);
        long maxmemory = runtime.maxMemory() / (1024 * 1024);
        sender.message(Colors.ORANGE + "Memory Used: " + Colors.GREEN + memused + "MB/" + maxmemory + "MB");
    }
}
