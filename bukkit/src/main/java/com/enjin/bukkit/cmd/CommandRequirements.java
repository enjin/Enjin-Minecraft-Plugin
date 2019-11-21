package com.enjin.bukkit.cmd;

import com.enjin.bukkit.EnjinMinecraftPlugin;
import com.enjin.bukkit.enums.MessageAction;
import com.enjin.bukkit.enums.Permission;
import com.enjin.bukkit.i18n.Translation;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CommandRequirements {

    protected EnjinMinecraftPlugin plugin;
    protected List<SenderType> allowedSenderTypes = new ArrayList<>();
    protected Permission permission;
    protected boolean requireValidKey;

    public CommandRequirements(EnjinMinecraftPlugin plugin) {
        this.plugin = plugin;
    }

    public boolean areMet(CommandSender sender, MessageAction messageAction) {
        return areMet(sender, SenderType.type(sender), messageAction);
    }

    public boolean areMet(CommandContext context, MessageAction messageAction) {
        return areMet(context.sender, context.senderType, messageAction);
    }

    public boolean areMet(CommandSender sender, SenderType senderType, MessageAction messageAction) {
        boolean senderAllowed = isSenderAllowed(sender);
        boolean hasPermission = hasPermission(sender);
        boolean keyRequiredAndInvalid = requireValidKey && plugin.isAuthKeyInvalid();

        if (messageAction == MessageAction.SEND) {
            if (!senderAllowed)
                sendInvalidSenderTypeMessage(sender, senderType);
            if (!hasPermission)
                sendNoPermissionMessage(sender);
            if (keyRequiredAndInvalid)
                sendInvalidKeyMessage(sender);
        }

        return senderAllowed && hasPermission && !keyRequiredAndInvalid;
    }

    protected boolean isSenderAllowed(CommandContext context) {
        return isSenderAllowed(context.senderType);
    }

    protected boolean isSenderAllowed(CommandSender sender) {
        return isSenderAllowed(SenderType.type(sender));
    }

    protected boolean isSenderAllowed(SenderType type) {
        return allowedSenderTypes.contains(SenderType.ANY) || allowedSenderTypes.contains(type);
    }

    protected boolean hasPermission(CommandContext context) {
        return hasPermission(context.sender);
    }

    protected boolean hasPermission(CommandSender sender) {
        return permission == null || permission.hasPermission(sender);
    }

    protected void sendInvalidSenderTypeMessage(CommandSender sender, SenderType senderType) {
        if (senderType == SenderType.PLAYER)
            Translation.CommandApi_Requirements_InvalidSenderTypePlayer.send(sender);
        else if (senderType == SenderType.CONSOLE)
            Translation.CommandApi_Requirements_InvalidSenderTypeConsole.send(sender);
        else if (senderType == SenderType.REMOTE_CONSOLE)
            Translation.CommandApi_Requirements_InvalidSenderTypeRemote.send(sender);
        else if (senderType == SenderType.BLOCK)
            Translation.CommandApi_Requirements_InvalidSenderTypeBlock.send(sender);
    }

    protected void sendInvalidKeyMessage(CommandSender sender) {
        Translation.CommandApi_Requirements_KeyRequiredAndInvalid.send(sender);
    }

    protected void sendNoPermissionMessage(CommandSender sender) {
        Translation.CommandApi_Requirements_NoPermission.send(sender, permission.node());
    }

    public static class Builder {

        private CommandRequirements requirements;

        public Builder(EnjinMinecraftPlugin plugin) {
            requirements = new CommandRequirements(plugin);
            requirements.allowedSenderTypes.add(SenderType.ANY);
        }

        public Builder withPermission(Permission permission) {
            requirements.permission = permission;
            return this;
        }

        public Builder withAllowedSenderTypes(SenderType... types) {
            requirements.allowedSenderTypes.clear();
            if (types != null && types.length != 0)
                requirements.allowedSenderTypes.addAll(Arrays.asList(types));
            return this;
        }

        public Builder requireValidKey() {
            requirements.requireValidKey = true;
            return this;
        }

        public CommandRequirements build() {
            return requirements;
        }

    }

    public static Builder builder(EnjinMinecraftPlugin plugin) {
        if (plugin == null)
            throw new NullPointerException();
        return new Builder(plugin);
    }

}
