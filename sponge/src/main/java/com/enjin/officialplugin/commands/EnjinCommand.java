package com.enjin.officialplugin.commands;

import org.spongepowered.api.text.Texts;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.util.command.CommandException;
import org.spongepowered.api.util.command.CommandResult;
import org.spongepowered.api.util.command.CommandSource;
import org.spongepowered.api.util.command.args.CommandContext;
import org.spongepowered.api.util.command.spec.CommandExecutor;

public class EnjinCommand implements CommandExecutor {
    @Override
    public CommandResult execute(CommandSource source, CommandContext context) throws CommandException {
        source.sendMessage(
                Texts.builder("/enjin setkey <key>").color(TextColors.GOLD).build()
        );
        return CommandResult.success();
    }
}
