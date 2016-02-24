package com.enjin.sponge.command.old.configuration;

import com.enjin.core.Enjin;
import com.enjin.core.EnjinServices;
import com.enjin.rpc.mappings.mappings.general.RPCData;
import com.enjin.rpc.mappings.services.PluginService;
import com.enjin.sponge.EnjinMinecraftPlugin;
import com.google.common.base.Optional;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandExecutor;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

public class SetKeyCommand implements CommandExecutor {
    @Override
    public CommandResult execute(CommandSource sender, CommandContext context) throws CommandException {
        java.util.Optional<String> key = context.<String>getOne("key");

        if (!key.isPresent()) {
            sender.sendMessage(Text.builder("Missing argument: key").color(TextColors.RED).build());
        } else {
            Sponge.getScheduler().createTaskBuilder().execute(() -> {
                if (Enjin.getConfiguration().getAuthKey().equals(key.get())) {
                    sender.sendMessage(Text.builder("That key has already been validated.").color(TextColors.RED).build());
                    return;
                }

                PluginService service = EnjinServices.getService(PluginService.class);
                RPCData<Boolean> data = service.auth(Optional.of(key.get()), EnjinMinecraftPlugin.getInstance().getPort(), true);

                if (data == null) {
                    sender.sendMessage(Text.builder("A fatal error has occurred. Please try again later. If the problem persists please contact Enjin support.").color(TextColors.RED).build());
                    return;
                }

                if (data.getError() != null) {
                    sender.sendMessage(Text.builder(data.getError().getMessage()).color(TextColors.RED).build());
                    return;
                }

                if (data.getResult().booleanValue()) {
                    sender.sendMessage(Text.builder("The key has been successfully validated.").color(TextColors.GREEN).build());
                    Enjin.getConfiguration().setAuthKey(key.get());
                    EnjinMinecraftPlugin.saveConfiguration();

                    if (EnjinMinecraftPlugin.getInstance().isAuthKeyInvalid()) {
                        EnjinMinecraftPlugin.getInstance().setAuthKeyInvalid(false);
                        EnjinMinecraftPlugin.getInstance().init();
                    }
                } else {
                    sender.sendMessage(Text.builder("We were unable to validate the provided key.").color(TextColors.RED).build());
                }
            }).async().submit(EnjinMinecraftPlugin.getInstance());
        }

        return CommandResult.success();
    }
}
