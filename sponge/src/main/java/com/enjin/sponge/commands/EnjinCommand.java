package com.enjin.sponge.commands;

import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

public class EnjinCommand implements CommandExecutor {
    @Override
    public CommandResult execute(CommandSource source, CommandContext context) throws CommandException {
        source.sendMessage(
                Text.builder("/enjin setkey <key>").color(TextColors.GOLD).build()
        );
        return CommandResult.success();
    }
}
