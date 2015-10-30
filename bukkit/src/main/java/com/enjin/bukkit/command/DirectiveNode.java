package com.enjin.bukkit.command;

import com.enjin.core.Enjin;
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
    @Getter
    private Permission permission;
    private Method method;

    public DirectiveNode(Directive data, Method method) {
        this.data = data;
        this.method = method;
    }

    public DirectiveNode(Directive data, Method method, Permission permission) {
        this(data, method);
        this.permission = permission;
    }

    public void invoke(CommandSender sender, String[] args) {
        if (method == null) {
            return;
        }

        if (!sender.isOp() && permission != null && permission.permission().equals("") && !sender.hasPermission(permission.permission())) {
            sender.sendMessage(ChatColor.RED + "You need to have the \"" + permission.permission() + "\" or OP to run that command.");
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

            Enjin.getPlugin().debug("Executing directive: " + data.parent() + "-" + data.directive());
            method.invoke(null, sender, args);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }
}
