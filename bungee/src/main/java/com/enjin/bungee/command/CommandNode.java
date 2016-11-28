package com.enjin.bungee.command;

import com.enjin.bungee.EnjinMinecraftPlugin;
import com.enjin.core.Enjin;
import com.google.common.collect.Maps;
import lombok.Getter;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.command.ConsoleCommandSender;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Map;

public class CommandNode extends net.md_5.bungee.api.plugin.Command {
    @Getter
    private Command data;
    @Getter
    private Permission perm;
    private Method method;
    @Getter
    private Map<String, DirectiveNode> directives = Maps.newHashMap();

    public CommandNode(Command data, Method method) {
        super(data.value(), null, data.aliases());
        this.data = data;
        this.method = method;
    }

    public CommandNode(Command data, Method method, Permission permission) {
        this(data, method);
        this.perm = permission;
    }

    public void invoke(CommandSender sender, String[] args) {
        if (method == null) {
            return;
        }

        if (sender instanceof ProxiedPlayer && perm != null && !perm.value().equals("") && !sender.hasPermission(perm.value())) {
            sender.sendMessage(ChatColor.RED + "You need to have the \"" + ChatColor.GOLD + perm.value() + ChatColor.RED + "\" or OP to run that command.");
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
            if (method.getParameterTypes()[0] == ProxiedPlayer.class && !(sender instanceof ProxiedPlayer)) {
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
            Enjin.getLogger().log(e);
        }
    }

    @Override
    public void execute(CommandSender commandSender, String[] args) {
        invoke(commandSender, args);
    }
}
