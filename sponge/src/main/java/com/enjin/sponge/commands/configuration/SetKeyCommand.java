package com.enjin.sponge.commands.configuration;

import com.enjin.core.Enjin;
import com.enjin.core.EnjinServices;
import com.enjin.rpc.mappings.mappings.general.RPCData;
import com.enjin.rpc.mappings.services.PluginService;
import com.enjin.sponge.EnjinMinecraftPlugin;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.text.Texts;
import org.spongepowered.api.text.format.TextColors;

import java.util.Optional;

public class SetKeyCommand implements CommandExecutor {
    @Override
    public CommandResult execute(CommandSource sender, CommandContext context) throws CommandException {
        Optional<String> key = context.<String>getOne("key");

        if (!key.isPresent()) {
            sender.sendMessage(Texts.builder("Missing argument: key").color(TextColors.RED).build());
        } else {
            Sponge.getScheduler().createTaskBuilder().execute(() -> {
                if (Enjin.getConfiguration().getAuthKey().equals(key.get())) {
                    sender.sendMessage(Texts.builder("That key has already been validated.").color(TextColors.RED).build());
                    return;
                }

                PluginService service = EnjinServices.getService(PluginService.class);
                RPCData<Boolean> data = service.auth(Optional.of(key.get()), EnjinMinecraftPlugin.getInstance().getPort(), true);

                if (data == null) {
                    sender.sendMessage(Texts.builder("A fatal error has occurred. Please try again later. If the problem persists please contact Enjin support.").color(TextColors.RED).build());
                    return;
                }

                if (data.getError() != null) {
                    sender.sendMessage(Texts.builder(data.getError().getMessage()).color(TextColors.RED).build());
                    return;
                }

                if (data.getResult().booleanValue()) {
                    sender.sendMessage(Texts.builder("The key has been successfully validated.").color(TextColors.GREEN).build());
                    Enjin.getConfiguration().setAuthKey(key.get());
                    EnjinMinecraftPlugin.saveConfiguration();

                    if (EnjinMinecraftPlugin.getInstance().isAuthKeyInvalid()) {
                        EnjinMinecraftPlugin.getInstance().setAuthKeyInvalid(false);
                        EnjinMinecraftPlugin.getInstance().init();
                    }
                } else {
                    sender.sendMessage(Texts.builder("We were unable to validate the provided key.").color(TextColors.RED).build());
                }
            }).async().submit(EnjinMinecraftPlugin.getInstance());
        }

        return CommandResult.success();
    }
}
