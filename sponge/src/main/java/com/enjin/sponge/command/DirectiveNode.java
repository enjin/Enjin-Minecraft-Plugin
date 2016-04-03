package com.enjin.sponge.command;

import com.enjin.core.Enjin;
import com.enjin.sponge.EnjinMinecraftPlugin;
import lombok.Getter;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.source.ConsoleSource;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

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

    public void invoke(CommandSource sender, String[] args) {
        if (method == null) {
            return;
        }

        if (permission != null && !permission.value().equals("") && !sender.hasPermission(permission.value())) {
            sender.sendMessage(Text.of(TextColors.RED, "You need to have the \"", TextColors.GOLD, permission.value(), TextColors.RED, "\" to run that directive."));
            return;
        }

        try {
            if (method.getParameterTypes()[0] == Player.class && !(sender instanceof Player)) {
                sender.sendMessage(Text.of(TextColors.RED, "This directive can only be used in-game by a player."));
                return;
            }

            if (method.getParameterTypes()[0] == ConsoleSource.class && !(sender instanceof ConsoleSource)) {
                sender.sendMessage(Text.of(TextColors.RED, "This directive can only be used by the console."));
                return;
            }

            if (EnjinMinecraftPlugin.getInstance().isAuthKeyInvalid() && data.requireValidKey()) {
                sender.sendMessage(Text.of(TextColors.RED, "This directive requires the server to successfully be authenticated with Enjin."));
                return;
            }

            Enjin.getPlugin().debug("Executing directive: " + data.parent() + "-" + data.value());
            method.invoke(null, sender, args);
        } catch (IllegalAccessException e) {
			Enjin.getLogger().catching(e);
        } catch (InvocationTargetException e) {
			Enjin.getLogger().catching(e);
        }
    }
}
