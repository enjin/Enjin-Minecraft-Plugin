package com.enjin.sponge.command.commands;

import com.enjin.core.Enjin;
import com.enjin.core.EnjinServices;
import com.enjin.rpc.mappings.mappings.general.RPCData;
import com.enjin.rpc.mappings.mappings.plugin.TagData;
import com.enjin.rpc.mappings.services.PluginService;
import com.enjin.sponge.EnjinMinecraftPlugin;
import com.enjin.sponge.command.Command;
import com.enjin.sponge.command.Directive;
import com.enjin.sponge.command.Permission;
import com.enjin.sponge.config.EMPConfig;
import com.enjin.sponge.config.RankUpdatesConfig;
import com.enjin.sponge.listeners.ConnectionListener;
import com.enjin.sponge.tasks.ReportPublisher;
import com.enjin.sponge.tasks.TPSMonitor;
import com.enjin.sponge.utils.io.EnjinConsole;
import com.enjin.sponge.utils.text.TextUtils;
import com.google.common.base.Optional;
import org.spongepowered.api.Platform;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.profile.GameProfile;
import org.spongepowered.api.service.user.UserStorageService;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.channel.MessageChannel;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.world.World;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

public class CoreCommands {
    @Command(value = "enjin", aliases = "e", requireValidKey = false)
    public static void enjin(CommandSource sender, String[] args) {
        sender.sendMessage(EnjinConsole.header());

        if (sender.hasPermission("enjin.setkey")) {
            sender.sendMessage(Text.of(TextColors.GOLD, "/enjin key <KEY>: ", TextColors.RESET, "Enter the secret key from your ", TextColors.GRAY, "Admin - Games - Minecraft - Enjin Plugin ", TextColors.RESET, "page."));
        }

        if (sender.hasPermission("enjin.inform")) {
            sender.sendMessage(Text.of(TextColors.GOLD, "/enjin inform <player> <MESSAGE>: ", TextColors.RESET, "Send a private message to a player."));
        }

        if (sender.hasPermission("enjin.broadcast")) {
            sender.sendMessage(Text.of(TextColors.GOLD, "/enjin broadcast <MESSAGE>: ", TextColors.RESET, "Broadcast a message to all players."));
        }

        if (sender.hasPermission("enjin.push")) {
            sender.sendMessage(Text.of(TextColors.GOLD, "/enjin push: ", TextColors.RESET, "Sync your website tags with the current ranks."));
        }

        if (sender.hasPermission("enjin.lag")) {
            sender.sendMessage(Text.of(TextColors.GOLD, "/enjin lag: ", TextColors.RESET, "Display TPS average and memory usage."));
        }

        if (sender.hasPermission("enjin.debug")) {
            sender.sendMessage(Text.of(TextColors.GOLD, "/enjin debug: ", TextColors.RESET, "Enable debug mode and display extra information in console."));
        }

        if (sender.hasPermission("enjin.report")) {
            sender.sendMessage(Text.of(TextColors.GOLD, "/enjin report: ", TextColors.RESET, "Generate a report file that you can send to Enjin Support for troubleshooting."));
        }

        if (sender.hasPermission("enjin.sign.set")) {
            sender.sendMessage(Text.of(TextColors.GOLD, "/enjin heads: ", TextColors.RESET, "Shows in game help for the heads and sign stats part of the plugin."));
        }

        if (sender.hasPermission("enjin.tags.view")) {
            sender.sendMessage(Text.of(TextColors.GOLD, "/enjin tags <player>: ", TextColors.RESET, "Shows the tags on the website for the player."));
        }

        // Points commands
        if (sender.hasPermission("enjin.points.getself")) {
            sender.sendMessage(Text.of(TextColors.GOLD, "/enjin points: ", TextColors.RESET, "Shows your current website points."));
        }

        if (sender.hasPermission("enjin.points.getothers")) {
            sender.sendMessage(Text.of(TextColors.GOLD, "/enjin points <NAME>: ", TextColors.RESET, "Shows another player's current website points."));
        }

        if (sender.hasPermission("enjin.points.add")) {
            sender.sendMessage(Text.of(TextColors.GOLD, "/enjin addpoints <NAME> <AMOUNT>: ", TextColors.RESET, "Add points to a player."));
        }

        if (sender.hasPermission("enjin.points.remove")) {
            sender.sendMessage(Text.of(TextColors.GOLD, "/enjin removepoints <NAME> <AMOUNT>: ", TextColors.RESET, "Remove points from a player."));
        }

        if (sender.hasPermission("enjin.points.set")) {
            sender.sendMessage(Text.of(TextColors.GOLD, "/enjin setpoints <NAME> <AMOUNT>: ", TextColors.RESET, "Set a player's total points."));
        }

        if (sender.hasPermission("enjin.support")) {
            sender.sendMessage(Text.of(TextColors.GOLD, "/enjin support: ", TextColors.RESET, "Starts ticket session or informs player of available modules."));
        }

        if (sender.hasPermission("enjin.ticket.self")) {
            sender.sendMessage(Text.of(TextColors.GOLD, "/enjin ticket: ", TextColors.RESET, "Sends player a list of their tickets."));
        }

        if (sender.hasPermission("enjin.ticket.open")) {
            sender.sendMessage(Text.of(TextColors.GOLD, "/enjin openticket: ", TextColors.RESET, "Sends player a list of open tickets."));
        }

        if (sender.hasPermission("enjin.ticket.reply")) {
            sender.sendMessage(Text.of(TextColors.GOLD, "/enjin reply <module #> <ticket id> <message>: ", TextColors.RESET, "Sends a reply to a ticket."));
        }

        if (sender.hasPermission("enjin.ticket.status")) {
            sender.sendMessage(Text.of(TextColors.GOLD, "/enjin ticketstatus <module #> <ticket id> <open|pending|closed>: ", TextColors.RESET, "Sets the status of a ticket."));
        }

        // Shop buy commands
        sender.sendMessage(Text.of(TextColors.GOLD, "/buy: ", TextColors.RESET, "Display items available for purchase."));
        sender.sendMessage(Text.of(TextColors.GOLD, "/buy page <#>: ", TextColors.RESET, "View the next page of results."));
        sender.sendMessage(Text.of(TextColors.GOLD, "/buy <ID>: ", TextColors.RESET, "Purchase the specified item ID in the server shop."));
        sender.sendMessage(Text.of(TextColors.GOLD, "/buy shop [ID] ", TextColors.RESET, "Shows the shop menu or opens the shop with the specified ID."));
    }

    @Permission(value = "enjin.broadcast")
    @Directive(parent = "enjin", value = "broadcast", requireValidKey = false)
    public static void broadcast(CommandSource sender, String[] args) {
        if (args.length < 1) {
            sender.sendMessage(Text.of(TextColors.RED, "To broadcast a message do: /enjin broadcast <message>"));
            return;
        }

        StringBuilder message = new StringBuilder();
        for (int i = 0; i < args.length; i++) {
            if (i > 0) {
                message.append(" ");
            }

            message.append(args[i]);
        }

        MessageChannel.TO_ALL.send(Text.of(TextUtils.translateText(message.toString())));
    }

    @Permission(value = "enjin.debug")
    @Directive(parent = "enjin", value = "debug", requireValidKey = false)
    public static void debug(CommandSource sender, String[] args) {
        EMPConfig config = Enjin.getConfiguration(EMPConfig.class);
        config.setDebug(!config.isDebug());
        EnjinMinecraftPlugin.saveConfiguration();

        sender.sendMessage(Text.of(TextColors.GREEN, "Debugging has been set to ", config.isDebug()));
    }

    @Permission(value = "enjin.inform")
    @Directive(parent = "enjin", value = "inform", requireValidKey = false)
    public static void inform(CommandSource sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(Text.of(TextColors.RED, "To send a message do: /enjin inform <player> <message>"));
            return;
        }

        Player player = Sponge.getServer().getPlayer(args[0]).get();
        for (Player p : Sponge.getServer().getOnlinePlayers()) {
            if (p.getName().equalsIgnoreCase(args[0]) || p.getUniqueId().toString().equalsIgnoreCase(args[0]) || p.getUniqueId().toString().replace("-", "").equals(args[0])) {
                player = p;
            }
        }

        if (player == null || !player.isOnline()) {
            sender.sendMessage(Text.of(TextColors.RED, args[0] + " isn't online at the moment."));
            return;
        }

        StringBuilder message = new StringBuilder();
        for (int i = 1; i < args.length; i++) {
            if (i > 1) {
                message.append(' ');
            }

            message.append(args[i]);
        }

        player.sendMessage(Text.of(TextUtils.translateText(message.toString())));
        sender.sendMessage(Text.of(TextColors.GREEN, "Your have successfully informed ", player.getName()));
    }

    @Permission(value = "enjin.setkey")
    @Command(value = "enjinkey", aliases = "ek", requireValidKey = false)
    @Directive(parent = "enjin", value = "key", aliases = {"setkey", "sk", "enjinkey", "ek"}, requireValidKey = false)
    public static void key(final CommandSource sender, final String[] args) {
        if (args.length != 1) {
            sender.sendMessage(Text.of("USAGE: /enjin key <key>"));
            return;
        }

        Enjin.getLogger().info("Checking if key is valid");

        Runnable runnable = () -> {
            if (Enjin.getConfiguration().getAuthKey().equals(args[0])) {
                sender.sendMessage(Text.of(TextColors.GREEN, "That key has already been validated."));
                return;
            }

            if (args[0].length() != 50) {
                sender.sendMessage(Text.of(TextColors.RED, "That authentication key is not 50 characters in length."));
                return;
            }

            PluginService service = EnjinServices.getService(PluginService.class);
            RPCData<Boolean> data = service.auth(Optional.of(args[0]), EnjinMinecraftPlugin.getInstance().getPort(), true);

            if (data == null) {
                sender.sendMessage(Text.of("A fatal error has occurred. Please try again later. If the problem persists please contact Enjin support."));
                return;
            }

            if (data.getError() != null) {
                sender.sendMessage(Text.of(TextColors.RED, data.getError().getMessage()));
                return;
            }

            if (data.getResult().booleanValue()) {
                sender.sendMessage(Text.of(TextColors.GREEN, "The key has been successfully validated."));
                Enjin.getConfiguration().setAuthKey(args[0]);
                EnjinMinecraftPlugin.saveConfiguration();
                EnjinMinecraftPlugin.getInstance().setAuthKeyInvalid(false);
                EnjinMinecraftPlugin.getInstance().init();
            } else {
                sender.sendMessage(Text.of(TextColors.RED, "We were unable to validate the provided key."));
            }
        };

        EnjinMinecraftPlugin.getInstance().getGame().getScheduler().createTaskBuilder()
                .execute(runnable)
                .async()
                .submit(Enjin.getPlugin());
    }

    @Permission(value = "enjin.lag")
    @Directive(parent = "enjin", value = "lag", requireValidKey = false)
    public static void lag(CommandSource sender, String[] args) {
        TPSMonitor monitor = TPSMonitor.getInstance();

        sender.sendMessage(Text.of(TextColors.GOLD,
                "Average TPS: ",
                TextColors.GREEN,
                TPSMonitor.getDecimalFormat().format(monitor.getTPSAverage())));
        sender.sendMessage(Text.of(TextColors.GOLD,
                "Last TPS Measurement: ",
                TextColors.GREEN,
                TPSMonitor.getDecimalFormat().format(monitor.getLastTPSMeasurement())));

        Runtime runtime = Runtime.getRuntime();
        long memused = (runtime.maxMemory() - runtime.freeMemory()) / (1024 * 1024);
        long maxmemory = runtime.maxMemory() / (1024 * 1024);

        sender.sendMessage(Text.of(TextColors.GOLD,
                "Memory Used: ",
                TextColors.GREEN,
                memused, "MB/", maxmemory, "MB"));
    }

    @Permission(value = "enjin.push")
    @Directive(parent = "enjin", value = "push")
    public static void push(CommandSource sender, String[] args) {
        RankUpdatesConfig config = EnjinMinecraftPlugin.getRankUpdatesConfiguration();
        java.util.Optional<UserStorageService> service = Sponge.getServiceManager().provide(UserStorageService.class);
        if (service.isPresent()) {
            ConnectionListener.updatePlayersRanks(service.get().getAll().toArray(new GameProfile[]{}));

            int minutes = Double.valueOf(Math.ceil(((double) config.getPlayerPerms().size()) / 500.0D)).intValue();
            sender.sendMessage(Text.of(TextColors.GREEN, Integer.toString(config.getPlayerPerms().size()), " players have been queued for synchronization. This should take approximately ", minutes, " minutes", (minutes > 1 ? "s." : ".")));
        }
    }

    @Permission(value = "enjin.report")
    @Directive(parent = "enjin", value = "report", requireValidKey = false)
    public static void report(CommandSource sender, String[] args) {
        EnjinMinecraftPlugin plugin = EnjinMinecraftPlugin.getInstance();
        Date date = new Date();
        DateFormat format = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss z");

        sender.sendMessage(Text.of(TextColors.GREEN, "Please wait while we generate the report"));

        StringBuilder report = new StringBuilder();
        report.append("Enjin Debug Report generated on ")
                .append(format.format(date))
                .append('\n')
                .append("Enjin Minecraft Plugin Version: ")
                .append(plugin.getContainer().getVersion().get())
                .append('\n');

        Platform platform = Sponge.getPlatform();
        report.append("Minecraft Version: ")
                .append(platform.getMinecraftVersion().getName())
                .append('\n')
                .append("Sponge API Version: ")
                .append(platform.getApi().getVersion().get())
                .append('\n')
                .append("Sponge Implementation Version: ")
                .append(platform.getImplementation().getVersion().get())
                .append('\n')
                .append("Java Version: ")
                .append(System.getProperty("java.version"))
                .append(' ')
                .append(System.getProperty("java.vendor"))
                .append('\n')
                .append("Operating System: ")
                .append(System.getProperty("os.name"))
                .append(' ')
                .append(System.getProperty("os.version"))
                .append(' ')
                .append(System.getProperty("os.arch"))
                .append('\n');

        if (plugin.isAuthKeyInvalid()) {
            report.append("ERROR: The authentication key is invalid.")
                    .append('\n');
        }

        report.append('\n')
                .append("Plugins:")
                .append('\n');
        for (PluginContainer container : Sponge.getPluginManager().getPlugins()) {
            report.append(container.getName())
                    .append(" Version: ")
                    .append(container.getVersion().orElse("N/A"))
                    .append('\n');
        }

        report.append('\n')
                .append("Worlds:")
                .append('\n');
        for (World world : Sponge.getServer().getWorlds()) {
            report.append(world.getName())
                    .append('\n');
        }

        Sponge.getGame().getScheduler().createTaskBuilder()
                .execute(new ReportPublisher(sender, report))
                .async()
                .submit(plugin);
    }

    @Permission(value = "enjin.tags")
    @Directive(parent = "enjin", value = "tags", requireValidKey = true)
    public static void tags(CommandSource sender, String[] args) {
        if (args.length == 0) {
            sender.sendMessage(Text.of(TextColors.RED, "/enjin tags <player>"));
            return;
        }

        String name = args[0].substring(0, args[0].length() > 16 ? 16 : args[0].length());
        PluginService service = EnjinServices.getService(PluginService.class);
        RPCData<List<TagData>> data = service.getTags(name);

        if (data == null) {
            sender.sendMessage(Text.of(TextColors.RED, "A fatal error has occurred. Please try again later. If the problem persists please contact Enjin support."));
            return;
        }

        if (data.getError() != null) {
            sender.sendMessage(Text.of(TextColors.RED, data.getError().getMessage()));
            return;
        }

        List<TagData> tags = data.getResult();

        if (tags.size() == 0) {
            sender.sendMessage(Text.of(TextColors.RED, "The user ", name, " currently doesn't have any tags."));
            return;
        }

        Text.Builder builder = Text.builder();
        if (tags != null) {
            Iterator<TagData> iterator = tags.iterator();
            while (iterator.hasNext()) {
                if (!builder.toText().isEmpty()) {
                    builder.append(Text.of(TextColors.GOLD, ", "));
                }

                TagData tag = iterator.next();
                builder.append(Text.of(TextColors.GREEN, tag.getName()));
            }
        }

        builder.insert(0, Text.of(TextColors.GOLD, name, "'s Tags: "));
        sender.sendMessage(builder.build());
    }
}
