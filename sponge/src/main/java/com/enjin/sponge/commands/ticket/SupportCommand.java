package com.enjin.sponge.commands.ticket;

import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;

public class SupportCommand implements CommandExecutor {
    @Override
    public CommandResult execute(CommandSource sender, CommandContext context) throws CommandException {
        return CommandResult.success();
    }
}
