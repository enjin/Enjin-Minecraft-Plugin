package com.enjin.bukkit.cmd.legacy;

import com.enjin.bukkit.EnjinMinecraftPlugin;
import com.enjin.bukkit.util.PermissionsUtil;
import com.enjin.core.Enjin;
import lombok.Getter;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

import java.lang.reflect.Method;

public class DirectiveNode {
    @Getter
    private Directive  data;
    @Getter
    private Permission permission;
    private Method     method;

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

        if (!sender.isOp() && permission != null && !permission.value().equals("") && !PermissionsUtil.hasPermission(
                sender,
                permission.value())) {
            sender.sendMessage(ChatColor.RED + "You need to have the \"" + ChatColor.GOLD + permission.value() + ChatColor.RED + "\" or OP to run that directive.");
            return;
        }

        try {
            if (method.getParameterTypes()[0] == Player.class && !(sender instanceof Player)) {
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
            Enjin.getLogger().log(e);
        }
    }
}
