package com.enjin.sponge.command;

import com.enjin.core.Enjin;
import com.enjin.sponge.EnjinMinecraftPlugin;
import com.google.common.collect.Maps;
import lombok.Getter;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.source.ConsoleSource;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

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

    public void invoke(CommandSource sender, String[] args) {
        if (method == null) {
            return;
        }

        if (permission != null && !permission.value().equals("") && !sender.hasPermission(permission.value())) {
            sender.sendMessage(Text.of(TextColors.RED, "You need to have the \"", TextColors.GOLD, permission.value(), TextColors.RED, "\" to run that command."));
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
                sender.sendMessage(Text.of(TextColors.RED, "This command can only be used in-game by a player."));
                return;
            }

            if (method.getParameterTypes()[0] == ConsoleSource.class && !(sender instanceof ConsoleSource)) {
                sender.sendMessage(Text.of(TextColors.RED, "This command can only be used by the console."));
                return;
            }

            if (EnjinMinecraftPlugin.getInstance().isAuthKeyInvalid() && data.requireValidKey()) {
                sender.sendMessage(Text.of(TextColors.RED, "This command requires the server to successfully be authenticated with Enjin."));
                return;
            }

            Enjin.getLogger().debug("Executing command: " + data.value());
            method.invoke(null, sender, args);
        } catch (InvocationTargetException e) {
            Enjin.getLogger().log(e.getCause());
        } catch (Exception e) {
            Enjin.getLogger().log(e);
        }
    }
}
