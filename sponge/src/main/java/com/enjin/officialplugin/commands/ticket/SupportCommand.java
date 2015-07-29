package com.enjin.officialplugin.commands.ticket;

import org.spongepowered.api.util.command.CommandException;
import org.spongepowered.api.util.command.CommandResult;
import org.spongepowered.api.util.command.CommandSource;
import org.spongepowered.api.util.command.args.CommandContext;
import org.spongepowered.api.util.command.spec.CommandExecutor;

public class SupportCommand implements CommandExecutor {
    @Override
    public CommandResult execute(CommandSource sender, CommandContext context) throws CommandException {
        return CommandResult.success();
    }
}
