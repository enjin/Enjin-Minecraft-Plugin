package com.enjin.officialplugin.commands.configuration;

import com.enjin.officialplugin.utils.KeyVerifier;
import org.spongepowered.api.text.Texts;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.util.command.CommandException;
import org.spongepowered.api.util.command.CommandResult;
import org.spongepowered.api.util.command.CommandSource;
import org.spongepowered.api.util.command.args.CommandContext;
import org.spongepowered.api.util.command.spec.CommandExecutor;

import java.util.Optional;

public class SetKeyCommand implements CommandExecutor {
    @Override
    public CommandResult execute(CommandSource sender, CommandContext context) throws CommandException {
        Optional<String> key = context.<String>getOne("key");

        if (!key.isPresent()) {
            sender.sendMessage(Texts.builder("Missing argument: key").color(TextColors.RED).build());
        } else {
            Thread thread = new Thread(new KeyVerifier(key.get(), sender));
            thread.start();
        }

        return CommandResult.success();
    }
}
