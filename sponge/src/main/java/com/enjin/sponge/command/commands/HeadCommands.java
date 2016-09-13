package com.enjin.sponge.command.commands;

import com.enjin.sponge.EnjinMinecraftPlugin;
import com.enjin.sponge.command.Directive;
import com.enjin.sponge.command.Permission;
import com.enjin.sponge.managers.StatSignManager;
import com.enjin.sponge.utils.io.EnjinConsole;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

public class HeadCommands {
    @Permission("enjin.sign.set")
    @Directive(parent = "enjin", value = "head", aliases = {"heads"}, requireValidKey = false)
    public static void head(CommandSource sender, String[] args) {
        sender.sendMessage(EnjinConsole.header());
        sender.sendMessage(Text.of(TextColors.AQUA, "To set a sign with a head, just place the head, then place the sign either above or below it."));
        sender.sendMessage(Text.of(TextColors.AQUA, "To create a sign of a specific type just put the code on the first line. # denotes the number."));
        sender.sendMessage(Text.of(TextColors.AQUA, " Example: [donation2] would show the second most recent donation."));
        sender.sendMessage(Text.of(TextColors.AQUA, "If there are sub-types, those go on the second line of the sign."));
        sender.sendMessage(Text.of(TextColors.GOLD, "[donation#] ", TextColors.RESET, " - Most recent donation."));
        sender.sendMessage(Text.of(TextColors.GRAY, " Subtypes: ", TextColors.RESET, " Place the item id on the second line to only get donations for that package."));
        sender.sendMessage(Text.of(TextColors.GOLD, "[topvoter#] ", TextColors.RESET, " - Top voter of the month."));
        sender.sendMessage(Text.of(TextColors.GRAY, " Subtypes: ", TextColors.RESET, " day, week, month. Changes it to the top voter of the day/week/month."));
        sender.sendMessage(Text.of(TextColors.GOLD, "[voter#] ", TextColors.RESET, " - Most recent voter."));
        sender.sendMessage(Text.of(TextColors.GOLD, "[topplayer#] ", TextColors.RESET, " - Top player (gets data from module on website)."));
        sender.sendMessage(Text.of(TextColors.GOLD, "[topposter#] ", TextColors.RESET, " - Top poster on the forum."));
        sender.sendMessage(Text.of(TextColors.GOLD, "[toplikes#] ", TextColors.RESET, " - Top forum likes."));
        sender.sendMessage(Text.of(TextColors.GOLD, "[newmember#] ", TextColors.RESET, " - Latest player to sign up on the website."));
        sender.sendMessage(Text.of(TextColors.GOLD, "[toppoints#] ", TextColors.RESET, " - Which player has the most unspent points."));
        sender.sendMessage(Text.of(TextColors.GOLD, "[pointsspent#] ", TextColors.RESET, " - Player which has spent the most points overall."));
        sender.sendMessage(Text.of(TextColors.GRAY, " Subtypes: ", TextColors.RESET, " day, week, month. Changes the range to day/week/month."));
        sender.sendMessage(Text.of(TextColors.GOLD, "[moneyspent#] ", TextColors.RESET, " - Player which has spent the most money on the server overall."));
        sender.sendMessage(Text.of(TextColors.GRAY, " Subtypes: ", TextColors.RESET, " day, week, month. Changes the range to day/week/month."));
    }

    @Permission("enjin.updateheads")
    @Directive(parent = "enjin", value = "updateheads", requireValidKey = true)
    public static void update(final CommandSource sender, final String[] args) {
        EnjinMinecraftPlugin.getInstance().getAsync().execute(() -> {
            sender.sendMessage(Text.of(TextColors.GREEN, "Fetching stat sign updates."));
            if (StatSignManager.fetchStats()) {
                StatSignManager.update();
                sender.sendMessage(Text.of(TextColors.GREEN, "Stat signs have been updated."));
            }
        });
    }
}
