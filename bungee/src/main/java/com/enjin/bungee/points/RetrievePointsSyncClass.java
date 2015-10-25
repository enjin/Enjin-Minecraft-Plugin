package com.enjin.bungee.points;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;

public class RetrievePointsSyncClass implements Runnable {

    String playername;
    CommandSender sender;
    boolean self;

    public RetrievePointsSyncClass(CommandSender sender, String playername, boolean self) {
        this.playername = playername;
        this.sender = sender;
        this.self = self;
    }

    @Override
    public synchronized void run() {
        try {
            int amount = PointsAPI.getPointsForPlayer(playername);
            if (self) {
                sender.sendMessage(ChatColor.GREEN + "You have " + ChatColor.GOLD + String.valueOf(amount) + " points.");
            } else {
                sender.sendMessage(ChatColor.GREEN + playername + " has " + ChatColor.GOLD + String.valueOf(amount) + " points.");
            }
        } catch (PlayerDoesNotExistException e) {
            sender.sendMessage(ChatColor.DARK_RED + "Enjin Error: That player has not registered on the website yet! In order to use this feature the player must be added on the website.");
        } catch (ErrorConnectingToEnjinException e) {
            sender.sendMessage(ChatColor.DARK_RED + "Enjin Error: We're unable to connect to enjin at this current time, please try again later.");
        }
    }

}
