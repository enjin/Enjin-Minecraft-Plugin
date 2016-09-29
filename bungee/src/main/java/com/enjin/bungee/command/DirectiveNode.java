package com.enjin.bungee.command;

import com.enjin.bungee.EnjinMinecraftPlugin;
import com.enjin.core.Enjin;
import lombok.Getter;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.command.ConsoleCommandSender;

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

        if (sender instanceof ProxiedPlayer && permission != null && !permission.value().equals("") && !sender.hasPermission(permission.value())) {
            sender.sendMessage(ChatColor.RED + "You need to have the \"" + ChatColor.GOLD + permission.value() + ChatColor.RED + "\" or OP to run that directive.");
            return;
        }

        try {
            if (method.getParameterTypes()[0] == ProxiedPlayer.class && !(sender instanceof ProxiedPlayer)) {
                sender.sendMessage(ChatColor.RED + "This directive can only be used in-game by a player.");
                return;
            }

            if (method.getParameterTypes()[0] == ConsoleCommandSender.class && !(sender instanceof ConsoleCommandSender)) {
                sender.sendMessage(ChatColor.RED + "This directive can only be used by the console.");
                return;
            }

            if (EnjinMinecraftPlugin.getInstance().isAuthKeyInvalid() && data.requireValidKey()) {
                sender.sendMessage(ChatColor.RED + "This directive requires the server to successfully be authenticated with Enjin.");
                return;
            }

            Enjin.getLogger().debug("Executing directive: " + data.parent() + "-" + data.value());
            method.invoke(null, sender, args);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
