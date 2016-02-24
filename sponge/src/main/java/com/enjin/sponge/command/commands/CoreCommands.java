package com.enjin.sponge.command.commands;

import com.enjin.core.Enjin;
import com.enjin.core.EnjinServices;
import com.enjin.rpc.mappings.mappings.general.RPCData;
import com.enjin.rpc.mappings.services.PluginService;
import com.enjin.sponge.EnjinMinecraftPlugin;
import com.enjin.sponge.command.Command;
import com.enjin.sponge.command.Directive;
import com.enjin.sponge.command.Permission;
import com.enjin.sponge.config.EMPConfig;
import com.enjin.sponge.utils.io.EnjinConsole;
import com.google.common.base.Optional;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

public class CoreCommands {
    @Command(value = "enjin", aliases = "e", requireValidKey = false)
    public static void enjin(CommandSource sender, String[] args) {
        sender.sendMessage(EnjinConsole.header());

        if (sender.hasPermission("enjin.setkey")) {
            sender.sendMessage(Text.of(TextColors.GOLD, "/enjin key <KEY>: ", TextColors.RESET, "Enter the secret key from your ", TextColors.GRAY, "Admin - Games - Minecraft - Enjin Plugin ", TextColors.RESET, "page."));
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
    }

    @Permission(value = "enjin.debug")
    @Directive(parent = "enjin", value = "debug", requireValidKey = false)
    public static void debug(CommandSource sender, String[] args) {
        EMPConfig config = Enjin.getConfiguration(EMPConfig.class);
        config.setDebug(!config.isDebug());
        EnjinMinecraftPlugin.saveConfiguration();

        sender.sendMessage(Text.of(TextColors.GREEN, "Debugging has been set to ", config.isDebug()));
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

        Runnable runnable = (Runnable) () -> {
            if (Enjin.getConfiguration().getAuthKey().equals(args[0])) {
                sender.sendMessage(Text.of(TextColors.GREEN, "That key has already been validated."));
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
}
