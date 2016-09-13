package com.enjin.bukkit.command.commands;

import com.enjin.bukkit.EnjinMinecraftPlugin;
import com.enjin.bukkit.modules.impl.SignStatsModule;
import com.enjin.bukkit.util.io.EnjinConsole;
import com.enjin.bukkit.command.Directive;
import com.enjin.bukkit.command.Permission;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

public class HeadCommands {
    @Permission("enjin.sign.set")
    @Directive(parent = "enjin", value = "head", aliases = {"heads"}, requireValidKey = false)
    public static void head(CommandSender sender, String[] args) {
        sender.sendMessage(EnjinConsole.header());
        sender.sendMessage(ChatColor.AQUA + "To set a sign with a head, just place the head, then place the sign either above or below it.");
        sender.sendMessage(ChatColor.AQUA + "To create a sign of a specific type just put the code on the first line. # denotes the number.");
        sender.sendMessage(ChatColor.AQUA + " Example: [donation2] would show the second most recent donation.");
        sender.sendMessage(ChatColor.AQUA + "If there are sub-types, those go on the second line of the sign.");
        sender.sendMessage(ChatColor.GOLD + "[donation#] " + ChatColor.RESET + " - Most recent donation.");
        sender.sendMessage(ChatColor.GRAY + " Subtypes: " + ChatColor.RESET + " Place the item id on the second line to only get donations for that package.");
        sender.sendMessage(ChatColor.GOLD + "[topvoter#] " + ChatColor.RESET + " - Top voter of the month.");
        sender.sendMessage(ChatColor.GRAY + " Subtypes: " + ChatColor.RESET + " day, week, month. Changes it to the top voter of the day/week/month.");
        sender.sendMessage(ChatColor.GOLD + "[voter#] " + ChatColor.RESET + " - Most recent voter.");
        sender.sendMessage(ChatColor.GOLD + "[topplayer#] " + ChatColor.RESET + " - Top player (gets data from module on website).");
        sender.sendMessage(ChatColor.GOLD + "[topposter#] " + ChatColor.RESET + " - Top poster on the forum.");
        sender.sendMessage(ChatColor.GOLD + "[toplikes#] " + ChatColor.RESET + " - Top forum likes.");
        sender.sendMessage(ChatColor.GOLD + "[newmember#] " + ChatColor.RESET + " - Latest player to sign up on the website.");
        sender.sendMessage(ChatColor.GOLD + "[toppoints#] " + ChatColor.RESET + " - Which player has the most unspent points.");
        sender.sendMessage(ChatColor.GOLD + "[pointsspent#] " + ChatColor.RESET + " - Player which has spent the most points overall.");
        sender.sendMessage(ChatColor.GRAY + " Subtypes: " + ChatColor.RESET + " day, week, month. Changes the range to day/week/month.");
        sender.sendMessage(ChatColor.GOLD + "[moneyspent#] " + ChatColor.RESET + " - Player which has spent the most money on the server overall.");
        sender.sendMessage(ChatColor.GRAY + " Subtypes: " + ChatColor.RESET + " day, week, month. Changes the range to day/week/month.");
    }

    @Permission("enjin.updateheads")
    @Directive(parent = "enjin", value = "updateheads", requireValidKey = true)
    public static void update(final CommandSender sender, final String[] args) {
        final SignStatsModule module = EnjinMinecraftPlugin.getInstance().getModuleManager().getModule(SignStatsModule.class);
        if (module == null) {
            return;
        }

        Bukkit.getScheduler().runTaskAsynchronously(EnjinMinecraftPlugin.getInstance(), new Runnable() {
            @Override
            public void run() {
                sender.sendMessage(ChatColor.GREEN + "Fetching stat sign updates.");
                module.fetchStats();
                module.update();
                sender.sendMessage(ChatColor.GREEN + "Stat signs have been updated.");
            }
        });
    }
}
