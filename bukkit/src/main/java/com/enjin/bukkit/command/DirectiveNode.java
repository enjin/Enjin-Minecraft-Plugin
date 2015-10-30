package com.enjin.bukkit.command;

import lombok.Getter;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class DirectiveNode {
    @Getter
    private Directive data;
    private Method method;

    public DirectiveNode(Directive data, Method method) {
        this.data = data;
        this.method = method;
    }

    public void invoke(CommandSender sender, String[] args) {
        if (method == null) {
            return;
        }

        if (!data.permission().equals("") && !sender.hasPermission(data.permission())) {
            sender.sendMessage(ChatColor.RED + "You do not have permission to use this command.");
            return;
        }

        try {
            if (method.getParameters()[0].getType() == Player.class && !(sender instanceof Player)) {
                sender.sendMessage("This command can only be used in-game by a player.");
                return;
            }

            if (method.getParameters()[0].getType() == ConsoleCommandSender.class && !(sender instanceof ConsoleCommandSender)) {
                sender.sendMessage("This command can only be used by the console.");
                return;
            }

            method.invoke(null, sender, args);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }
}
