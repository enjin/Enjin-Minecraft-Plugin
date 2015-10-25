package com.enjin.bungee.points;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.connection.ProxiedPlayer;

public class EnjinPointsSyncClass implements Runnable {

    String playername;
    int points;
    PointsAPI.Type type;
    CommandSender sender;

    public EnjinPointsSyncClass(CommandSender sender, String playername, int points, PointsAPI.Type type) {
        this.playername = playername;
        this.points = points;
        this.type = type;
        this.sender = sender;
    }

    @Override
    public synchronized void run() {
        try {
            int amount = PointsAPI.modifyPointsToPlayer(playername, points, type);
            String addremove = "";
            String toplayer = "";
            switch (type) {
                case AddPoints:
                    addremove = "added";
                    toplayer = "added " + points + " points to your account!";
                    break;
                case RemovePoints:
                    addremove = "removed";
                    toplayer = "removed " + points + " points from your account!";
                    break;
                case SetPoints:
                    addremove = "set";
                    toplayer = "set your points balance.";
            }
            sender.sendMessage(ChatColor.DARK_GREEN + "Successfully " + addremove + " " + points + " points to player " + playername + "! The player now has " + amount + " points.");
            ProxiedPlayer p = ProxyServer.getInstance().getPlayer(playername);
            if (p != null) {
                p.sendMessage(ChatColor.GOLD + sender.getName() + ChatColor.YELLOW + " just " + toplayer + " You now have " + ChatColor.DARK_GREEN + amount + " points.");
            }
        } catch (NumberFormatException e) {
            sender.sendMessage(ChatColor.DARK_RED + "Enjin Error: Not a valid number!");
        } catch (PlayerDoesNotExistException e) {
            sender.sendMessage(ChatColor.DARK_RED + "Enjin Error: That player has not registered on the website yet! In order to use this feature the player must be added on the website.");
        } catch (ErrorConnectingToEnjinException e) {
            e.printStackTrace();
            sender.sendMessage(ChatColor.DARK_RED + "Enjin Error: We're unable to connect to enjin at this current time, please try again later.");
        }
    }

}
