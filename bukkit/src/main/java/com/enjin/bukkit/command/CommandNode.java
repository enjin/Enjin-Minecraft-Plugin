package com.enjin.bukkit.command;

import com.enjin.bukkit.EnjinMinecraftPlugin;
import com.enjin.bukkit.util.PermissionsUtil;
import com.enjin.core.Enjin;
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

        if (!sender.isOp() && permission != null && !permission.value().equals("") && !PermissionsUtil.hasPermission(sender, permission.value())) {
            sender.sendMessage(ChatColor.RED + "You need to have the \"" + ChatColor.GOLD + permission.value() + ChatColor.RED + "\" or OP to run that command.");
            return;
        }

        if (args.length > 0) {
            DirectiveNode directive = directives.get(args[0].toLowerCase());
            if (directive != null) {
                directive.invoke(sender, args.length > 1 ? Arrays.copyOfRange(args, 1, args.length) : new String[]{});
                return;
            }
        }

        try {
            if (method.getParameterTypes()[0] == Player.class && !(sender instanceof Player)) {
                sender.sendMessage(ChatColor.RED + "This command can only be used in-game by a player.");
                return;
            }

            if (method.getParameterTypes()[0] == ConsoleCommandSender.class && !(sender instanceof ConsoleCommandSender)) {
                sender.sendMessage(ChatColor.RED + "This command can only be used by the console.");
                return;
            }

            if (EnjinMinecraftPlugin.getInstance().isAuthKeyInvalid() && data.requireValidKey()) {
                sender.sendMessage(ChatColor.RED + "This command requires the server to successfully be authenticated with Enjin.");
                return;
            }

            Enjin.getLogger().debug("Executing command: " + data.value());
            method.invoke(null, sender, args);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
