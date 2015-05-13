package com.enjin.officialplugin.points;


import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ChatMessageComponent;

import com.enjin.officialplugin.ChatColor;
import com.enjin.officialplugin.points.PointsAPI.Type;

public class EnjinPointsSyncClass implements Runnable {

    String playername;
    int points;
    PointsAPI.Type type;
    ICommandSender sender;

    public EnjinPointsSyncClass(ICommandSender sender, String playername, int points, Type type) {
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
            sender.sendChatToPlayer(ChatMessageComponent.createFromText(ChatColor.DARK_GREEN + "Successfully " + addremove + " " + points + " points to player " + playername + "! The player now has " + amount + " points."));
            EntityPlayer p = PlayerUtil.getPlayer(playername);
            if (p != null) {
                p.addChatMessage(ChatColor.GOLD + sender.getCommandSenderName() + ChatColor.YELLOW + " just " + toplayer + " You now have " + ChatColor.DARK_GREEN + amount + " points.");
            }
        } catch (NumberFormatException e) {
            sender.sendChatToPlayer(ChatMessageComponent.createFromText(ChatColor.DARK_RED + "Enjin Error: Not a valid number!"));
        } catch (PlayerDoesNotExistException e) {
            sender.sendChatToPlayer(ChatMessageComponent.createFromText(ChatColor.DARK_RED + "Enjin Error: That player has not registered on the website yet! In order to use this feature the player must be added on the website."));
        } catch (ErrorConnectingToEnjinException e) {
            e.printStackTrace();
            sender.sendChatToPlayer(ChatMessageComponent.createFromText(ChatColor.DARK_RED + "Enjin Error: We're unable to connect to enjin at this current time, please try again later."));
        }
    }

}
