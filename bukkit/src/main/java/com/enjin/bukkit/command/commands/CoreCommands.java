package com.enjin.bukkit.command.commands;

import Tux2.TuxTwoLib.TuxTwoPlayer;
import com.enjin.bukkit.config.RankUpdatesConfig;
import com.enjin.bukkit.listeners.ConnectionListener;
import com.enjin.bukkit.tasks.TPSMonitor;
import com.enjin.bukkit.util.PermissionsUtil;
import com.enjin.bukkit.util.io.EnjinConsole;
import com.enjin.bukkit.EnjinMinecraftPlugin;
import com.enjin.bukkit.command.Command;
import com.enjin.bukkit.command.Directive;
import com.enjin.bukkit.command.Permission;
import com.enjin.bukkit.config.EMPConfig;
import com.enjin.bukkit.modules.impl.VaultModule;
import com.enjin.bukkit.tasks.ReportPublisher;
import com.enjin.core.Enjin;
import com.enjin.core.EnjinServices;
import com.enjin.rpc.mappings.mappings.general.RPCData;
import com.enjin.rpc.mappings.mappings.plugin.Auth;
import com.enjin.rpc.mappings.mappings.plugin.TagData;
import com.enjin.rpc.mappings.services.PluginService;
import com.google.common.base.Optional;
import org.bukkit.*;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.Repairable;
import org.bukkit.plugin.Plugin;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CoreCommands {
    private static LinkedList<String> keywords = new LinkedList<String>() {{
        add("-name");
        add("-color");
        add("-repairxp");
        add("--n");
        add("--c");
        add("--r");
    }};

    @Command(value = "enjin", aliases = "e", requireValidKey = false)
    public static void enjin(CommandSender sender, String[] args) {
        sender.sendMessage(EnjinConsole.header());

        if (PermissionsUtil.hasPermission(sender, "enjin.setkey")) {
            sender.sendMessage(ChatColor.GOLD + "/enjin key <KEY>: " + ChatColor.RESET + "Enter the secret key from your " + ChatColor.GRAY + "Admin - Games - Minecraft - Enjin Plugin " + ChatColor.RESET + "page.");
        }

        if (PermissionsUtil.hasPermission(sender, "enjin.broadcast")) {
            sender.sendMessage(ChatColor.GOLD + "/enjin broadcast <MESSAGE>: " + ChatColor.RESET + "Broadcast a message to all players.");
        }

        if (PermissionsUtil.hasPermission(sender, "enjin.push")) {
            sender.sendMessage(ChatColor.GOLD + "/enjin push: " + ChatColor.RESET + "Sync your website tags with the current ranks.");
        }

        if (PermissionsUtil.hasPermission(sender, "enjin.lag")) {
            sender.sendMessage(ChatColor.GOLD + "/enjin lag: " + ChatColor.RESET + "Display TPS average and memory usage.");
        }

        if (PermissionsUtil.hasPermission(sender, "enjin.debug")) {
            sender.sendMessage(ChatColor.GOLD + "/enjin debug: " + ChatColor.RESET + "Enable debug mode and display extra information in console.");
        }

        if (PermissionsUtil.hasPermission(sender, "enjin.report")) {
            sender.sendMessage(ChatColor.GOLD + "/enjin report: " + ChatColor.RESET + "Generate a report file that you can send to Enjin Support for troubleshooting.");
        }

        if (PermissionsUtil.hasPermission(sender, "enjin.sign.set")) {
            sender.sendMessage(ChatColor.GOLD + "/enjin heads: " + ChatColor.RESET + "Shows in game help for the heads and sign stats part of the plugin.");
        }

        if (PermissionsUtil.hasPermission(sender, "enjin.tags.view")) {
            sender.sendMessage(ChatColor.GOLD + "/enjin tags <player>: " + ChatColor.RESET + "Shows the tags on the website for the player.");
        }

        // Points commands
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

        // Shop buy commands
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

    @Permission(value = "enjin.broadcast")
    @Directive(parent = "enjin", value = "broadcast", requireValidKey = false)
    public static void broadcast(CommandSender sender, String[] args) {
        if (args.length < 1) {
            sender.sendMessage(ChatColor.RED + "To broadcast a message do: /enjin broadcast <message>");
            return;
        }

        StringBuilder message = new StringBuilder();
        for (int i = 0; i < args.length; i++) {
            if (i > 0) {
                message.append(" ");
            }

            message.append(args[i]);
        }

        Bukkit.broadcastMessage(ChatColor.translateAlternateColorCodes('&', message.toString()));
    }

    @Permission(value = "enjin.debug")
    @Directive(parent = "enjin", value = "debug", requireValidKey = false)
    public static void debug(CommandSender sender, String[] args) {
        EMPConfig config = Enjin.getConfiguration(EMPConfig.class);
        config.setDebug(!config.isDebug());
        EnjinMinecraftPlugin.saveConfiguration();

        Enjin.getLogger().setDebug(config.isDebug());

        sender.sendMessage(ChatColor.GREEN + "Debugging has been set to " + config.isDebug());
    }

    @Permission(value = "enjin.give")
    @Directive(parent = "enjin", value = "give", requireValidKey = false)
    public static void give(CommandSender sender, String[] args) {
        EnjinMinecraftPlugin plugin = EnjinMinecraftPlugin.getInstance();

        if (args.length < 2) {
            sender.sendMessage(ChatColor.RED + "Syntax: /enjin give <player|uuid> <material>");
            return;
        }

        Player player;
        String index = args[0].trim();
        UUID uuid = null;

        if (index.length() > 16) {
            if (index.length() == 32) {
                index = index.substring(0, 8) + "-" + index.substring(8, 12) + "-" + index.substring(12, 16) + "-" + index.substring(16, 20) + "-" + index.substring(20, 32);
            } else if (index.length() != 36) {
                sender.sendMessage(ChatColor.RED + "Invalid UUID");
                return;
            }

            try {
                uuid = UUID.fromString(index);
                player = Bukkit.getPlayer(uuid);
            } catch (IllegalArgumentException e) {
                sender.sendMessage(ChatColor.RED + "Invalid UUID");
                return;
            }
        } else {
            player = Bukkit.getPlayer(index);
        }

        boolean online = true;
        if (player == null || !player.isOnline()) {
            if (!plugin.isTuxTwoLibInstalled()) {
                sender.sendMessage(ChatColor.RED + "This player is not online. In order to give items to players not online please install TuxTwoLib");
                return;
            }

            OfflinePlayer offlinePlayer;

            if (uuid != null) {
                offlinePlayer = Bukkit.getOfflinePlayer(uuid);
            } else {
                offlinePlayer = Bukkit.getOfflinePlayer(index);
            }

            Player target = TuxTwoPlayer.getOfflinePlayer(offlinePlayer);

            if (target != null) {
                target.loadData();
                online = false;
                player = target;
            } else {
                sender.sendMessage(ChatColor.DARK_RED + "[Enjin] Player not found. Item not given.");
                return;
            }
        }

        try {
            int extradatastart = 3;
            Pattern digits = Pattern.compile("\\d+");
            if (args[1].contains(":")) {
                String[] split = args[1].split(":");
                ItemStack is;
                Pattern pattern = Pattern.compile("\\d+:\\d+");
                Matcher match = pattern.matcher(args[1]);

                if (match.find()) {
                    try {
                        int itemid = Integer.parseInt(split[0]);
                        int damage = Integer.parseInt(split[1]);
                        int quantity = 1;

                        if (args.length > 2 && digits.matcher(args[2]).find()) {
                            quantity = Integer.parseInt(args[2]);
                            extradatastart = 3;
                        }

                        is = new ItemStack(itemid, quantity, (short) damage);
                        sender.sendMessage(ChatColor.RED + "Using IDs is depreciated. Please switch to using material name: http://jd.bukkit.org/beta/apidocs/org/bukkit/Material.html");
                    } catch (NumberFormatException e) {
                        sender.sendMessage(ChatColor.DARK_RED + "Ooops, something went wrong. Did you specify the item correctly?");
                        return;
                    }
                } else {
                    try {
                        Material itemid = Material.getMaterial(split[0].trim().toUpperCase());

                        if (itemid == null) {
                            sender.sendMessage(ChatColor.DARK_RED + "Ooops, I couldn't find a material with that name. Did you spell it correctly?");
                            return;
                        }

                        int damage = Integer.parseInt(split[1]);
                        int quantity = 1;

                        if (args.length > 2 && digits.matcher(args[2]).find()) {
                            quantity = Integer.parseInt(args[2]);
                            extradatastart = 3;
                        }

                        is = new ItemStack(itemid, quantity, (short) damage);
                    } catch (NumberFormatException ex) {
                        sender.sendMessage(ChatColor.DARK_RED + "Ooops, something went wrong. Did you specify the item correctly?");
                        return;
                    }
                }

                if (args.length > extradatastart) {
                    addCustomData(is, args, player, extradatastart);
                }

                player.getInventory().addItem(is);

                if (!online) {
                    player.saveData();
                }

                String itemname = is.getType().toString().toLowerCase();
                sender.sendMessage(ChatColor.DARK_AQUA + "You just gave " + player.getName() + " " + is.getAmount() + " " + itemname.replace("_", " ") + "!");
            } else {
                ItemStack is;
                try {
                    int itemid = Integer.parseInt(args[1]);
                    int quantity = 1;

                    if (args.length > 2 && digits.matcher(args[2]).find()) {
                        quantity = Integer.parseInt(args[2]);
                        extradatastart = 3;
                    }

                    is = new ItemStack(itemid, quantity);
                    sender.sendMessage(ChatColor.RED + "Using IDs is depreciated. Please switch to using material name: http://jd.bukkit.org/beta/apidocs/org/bukkit/Material.html");
                } catch (NumberFormatException e) {
                    Material material = Material.getMaterial(args[1].trim().toUpperCase());

                    if (material == null) {
                        sender.sendMessage(ChatColor.DARK_RED + "Ooops, I couldn't find a material with that name. Did you spell it correctly?");
                        return;
                    }

                    int quantity = 1;

                    if (args.length > 2 && digits.matcher(args[2]).find()) {
                        quantity = Integer.parseInt(args[2]);
                        extradatastart = 3;
                    }

                    is = new ItemStack(material, quantity);
                }

                if (args.length > extradatastart) {
                    addCustomData(is, args, player, extradatastart);
                }

                player.getInventory().addItem(is);

                if (!online) {
                    player.saveData();
                }

                String itemname = is.getType().toString().toLowerCase();
                sender.sendMessage(ChatColor.DARK_AQUA + "You just gave " + player.getName() + " " + is.getAmount() + " " + itemname.replace("_", " ") + "!");
            }
        } catch (Exception e) {
            sender.sendMessage(ChatColor.DARK_RED + "Ooops, something went wrong. Did you specify the item correctly?");
        }
    }

    @Permission(value = "enjin.inform")
    @Directive(parent = "enjin", value = "inform", requireValidKey = false)
    public static void inform(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(ChatColor.RED + "To send a message do: /enjin inform <player> <message>");
            return;
        }

        Player player = null;
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (p.getName().equalsIgnoreCase(args[0]) || p.getUniqueId().toString().equalsIgnoreCase(args[0]) || p.getUniqueId().toString().replace("-", "").equals(args[0])) {
                player = p;
            }
        }

        if (player == null || !player.isOnline()) {
            if (sender instanceof Player) {
                sender.sendMessage(ChatColor.RED + args[0] + " isn't online at the moment.");
            } else {
                Enjin.getLogger().info(ChatColor.RED + args[0] + " isn't online at the moment.");
            }

            return;
        }

        StringBuilder message = new StringBuilder();
        for (int i = 1; i < args.length; i++) {
            if (i > 1) {
                message.append(" ");
            }

            message.append(args[i]);
        }

        player.sendMessage(ChatColor.translateAlternateColorCodes('&', message.toString()));
    }

    @Permission(value = "enjin.setkey")
    @Command(value = "enjinkey", aliases = "ek", requireValidKey = false)
    @Directive(parent = "enjin", value = "key", aliases = {"setkey", "sk", "enjinkey", "ek"}, requireValidKey = false)
    public static void key(final CommandSender sender, final String[] args) {
        if (args.length != 1) {
            sender.sendMessage("USAGE: /enjin key <key>");
            return;
        }

        Enjin.getLogger().info("Checking if key is valid");
        EnjinMinecraftPlugin.getInstance().getLogger().info("Checking if key is valid");

        Bukkit.getScheduler().runTaskAsynchronously(EnjinMinecraftPlugin.getInstance(), new Runnable() {
            @Override
            public void run() {
                if (Enjin.getConfiguration().getAuthKey().equals(args[0])) {
                    sender.sendMessage(ChatColor.GREEN + "That key has already been validated.");
                    return;
                }

                PluginService service = EnjinServices.getService(PluginService.class);
                RPCData<Auth> data = service.auth(Optional.of(args[0]), Bukkit.getPort(), true, true);

                if (data == null) {
                    sender.sendMessage(ChatColor.RED + "Unable to connect with Enjin web servers or an unexpected error occurred. Contact Enjin support if issues persist.");
                    return;
                }

                if (data.getError() != null) {
                    sender.sendMessage(ChatColor.RED + data.getError().getMessage());
                    return;
                }

                if (data.getResult() != null && data.getResult().isAuthed()) {
                    sender.sendMessage(ChatColor.GREEN + "The key has been successfully validated.");
                    Enjin.getConfiguration().setAuthKey(args[0]);
                    EnjinMinecraftPlugin.saveConfiguration();
                    EnjinMinecraftPlugin.getInstance().setAuthKeyInvalid(false);
                    EnjinMinecraftPlugin.getInstance().init();
                } else {
                    sender.sendMessage(ChatColor.RED + "We were unable to validate the provided key.");
                }
            }
        });
    }

    @Permission(value = "enjin.lag")
    @Directive(parent = "enjin", value = "lag", requireValidKey = false)
    public static void lag(CommandSender sender, String[] args) {
        TPSMonitor monitor = TPSMonitor.getInstance();

        sender.sendMessage(ChatColor.GOLD + "Average TPS: " + ChatColor.GREEN + TPSMonitor.getDecimalFormat().format(monitor.getTPSAverage()));
        sender.sendMessage(ChatColor.GOLD + "Last TPS measurement: " + ChatColor.GREEN + TPSMonitor.getDecimalFormat().format(monitor.getLastTPSMeasurement()));

        Runtime runtime = Runtime.getRuntime();
        long memused = (runtime.maxMemory() - runtime.freeMemory()) / (1024 * 1024);
        long maxmemory = runtime.maxMemory() / (1024 * 1024);

        sender.sendMessage(ChatColor.GOLD + "Memory Used: " + ChatColor.GREEN + memused + "MB/" + maxmemory + "MB");
    }

    @Permission(value = "enjin.push")
    @Directive(parent = "enjin", value = "push")
    public static void push(CommandSender sender, String[] args) {
        RankUpdatesConfig config = EnjinMinecraftPlugin.getRankUpdatesConfiguration();
        ConnectionListener.updatePlayersRanks(Bukkit.getOfflinePlayers());

        int minutes = Double.valueOf(Math.ceil(((double) config.getPlayerPerms().size()) / 500.0D)).intValue();
        sender.sendMessage(ChatColor.GREEN + Integer.toString(config.getPlayerPerms().size()) + " players have been queued for synchronization. This should take approximately " + minutes + " minutes" + (minutes > 1 ? "s." : "."));
    }

    @Permission(value = "enjin.report")
    @Directive(parent = "enjin", value = "report", requireValidKey = false)
    public static void report(CommandSender sender, String[] args) {
        EnjinMinecraftPlugin plugin = EnjinMinecraftPlugin.getInstance();

        Date date = new Date();
        DateFormat format = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss z");

        sender.sendMessage(ChatColor.GREEN + "Please wait while we generate the report");

        StringBuilder report = new StringBuilder();
        report.append("Enjin Debug Report generated on ").append(format.format(date)).append("\n");
        report.append("Enjin plugin version: ").append(plugin.getDescription().getVersion()).append("\n");


        VaultModule module = plugin.getModuleManager().getModule(VaultModule.class);
        if (module != null && Bukkit.getPluginManager().isPluginEnabled("Vault")) {
            Plugin permissions = null;
            if (module.getPermission() != null) {
                permissions = Bukkit.getPluginManager().getPlugin(module.getPermission().getName());
            }

            if (permissions != null) {
                report.append("Permissions plugin used: ").append(permissions.getDescription().getName()).append(" version ").append(permissions.getDescription().getVersion()).append("\n");
            }

            Plugin economy = null;
            if (module.getEconomy() != null) {
                economy = Bukkit.getPluginManager().getPlugin(module.getEconomy().getName());
            }

            if (economy != null) {
                report.append("Economy plugin used: ").append(economy.getDescription().getName()).append(" version ").append(economy.getDescription().getVersion()).append("\n");
            }
        }

        Plugin votifier = Bukkit.getPluginManager().getPlugin("Votifier");
        if (votifier != null) {
            report.append("Votifier version: ").append(votifier.getDescription().getVersion()).append("\n");
            FileConfiguration votifierConfig = votifier.getConfig();
            String port = votifierConfig.getString("port", "");
            String host = votifierConfig.getString("host", "");
            report.append("Votifier Enabled: ").append(votifier.isEnabled()).append("\n");
            if (!port.isEmpty() && !host.isEmpty()) {
                report.append("Votifier is listening on: ").append(host).append(":").append(port).append("\n");
            }
        }

        report.append("Bukkit version: ").append(Bukkit.getVersion()).append("\n");
        report.append("Java version: ").append(System.getProperty("java.version")).append(" ").append(System.getProperty("java.vendor")).append("\n");
        report.append("Operating system: ").append(System.getProperty("os.name")).append(" ").append(System.getProperty("os.version")).append(" ").append(System.getProperty("os.arch")).append("\n");

        if (plugin.isAuthKeyInvalid()) {
            report.append("ERROR: Authkey reported by plugin as invalid!\n");
        }

        report.append("Enjin Server ID: ")
                .append(plugin.getServerId())
                .append('\n');

        if (plugin.isUnableToContactEnjin()) {
            report.append("WARNING: Plugin has been unable to contact Enjin for the past 5 minutes\n");
        }

        if (plugin.isPermissionsNotWorking()) {
            report.append("WARNING: Permissions plugin is not configured properly and is disabled. Check the server.log for more details.\n");
        }

        report.append("\nPlugins: \n");
        for (Plugin p : Bukkit.getPluginManager().getPlugins()) {
            report.append(p.getName()).append(" version ").append(p.getDescription().getVersion()).append("\n");
        }

        report.append("\nWorlds: \n");
        for (World world : Bukkit.getWorlds()) {
            report.append(world.getName()).append("\n");
        }

        Bukkit.getScheduler().runTaskAsynchronously(plugin, new ReportPublisher(plugin, report, sender));
    }

    @Permission(value = "enjin.tags")
    @Directive(parent = "enjin", value = "tags", requireValidKey = true)
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

        if (tags.size() == 0) {
            sender.sendMessage(ChatColor.RED + "The user " + name + " currently doesn't have any tags.");
            return;
        }

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

    private static void addCustomData(ItemStack is, String[] args, OfflinePlayer reciever, int startingpos) {
        for (int i = startingpos; i < args.length; i++) {
            if (args[i].equalsIgnoreCase("-name") || args[i].equalsIgnoreCase("--n")) {
                boolean noflags = true;
                i++;
                StringBuilder name = new StringBuilder();
                while (noflags && i < args.length) {
                    if (keywords.contains(args[i].toLowerCase())) {
                        noflags = false;
                        i--;
                    } else {
                        name.append(args[i]).append(" ");
                        i++;
                    }
                }
                addName(is, ChatColor.translateAlternateColorCodes('&', name.toString().trim()));
            } else if (args[i].equalsIgnoreCase("-color") || args[i].equalsIgnoreCase("--c")) {
                i++;
                if (args.length > i) {
                    try {
                        String[] rgb = args[i].split(",");
                        int r = 0;
                        int g = 0;
                        int b = 0;
                        for (String col : rgb) {
                            col = col.toLowerCase();
                            if (col.startsWith("r")) {
                                r = Integer.parseInt(col.substring(1));
                            } else if (col.startsWith("g")) {
                                g = Integer.parseInt(col.substring(1));
                            } else if (col.startsWith("b")) {
                                b = Integer.parseInt(col.substring(1));
                            }
                        }
                        ItemMeta meta = is.getItemMeta();
                        if (meta instanceof LeatherArmorMeta) {
                            ((LeatherArmorMeta) meta).setColor(Color.fromRGB(r, g, b));
                            is.setItemMeta(meta);
                        }
                    } catch (NumberFormatException ignored) {

                    }
                }
            } else if (args[i].equalsIgnoreCase("-repairxp") || args[i].equalsIgnoreCase("--r")) {
                i++;
                if (args.length > i) {
                    try {
                        int repaircost = Integer.parseInt(args[i]);
                        ItemMeta meta = is.getItemMeta();
                        if (meta instanceof Repairable) {
                            ((Repairable) meta).setRepairCost(repaircost);
                            is.setItemMeta(meta);
                        }
                    } catch (NumberFormatException ignored) {

                    }
                }
            }
        }
    }

    private static ItemStack addName(ItemStack is, String name) {
        ItemMeta meta = is.getItemMeta();
        meta.setDisplayName(name);
        is.setItemMeta(meta);
        return is;
    }
}
