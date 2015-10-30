package com.enjin.bukkit.command;

import com.google.common.collect.Maps;
import lombok.Getter;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Map;

public class CommandNode {
    @Getter
    private Command data;
    @Getter
    private Permission permission;
    private Method method;
    @Getter
    private Map<String, DirectiveNode> directives = Maps.newHashMap();

    public CommandNode(Command data, Method method) {
        this.data = data;
        this.method = method;
    }

    public CommandNode(Command data, Method method, Permission permission) {
        this(data, method);
        this.permission = permission;
    }

    public void invoke(CommandSender sender, String[] args) {
        if (method == null) {
            return;
        }

        if (permission != null && permission.permission().equals("") && !sender.hasPermission(permission.permission())) {
            sender.sendMessage(ChatColor.RED + "You do not have permission to use this command.");
            return;
        }

        if (args.length > 0) {
            DirectiveNode directive = directives.get(args[0]);
            if (directive != null) {
                directive.invoke(sender, args.length > 1 ? Arrays.copyOfRange(args, 1, args.length) : new String[]{});
                return;
            }
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
