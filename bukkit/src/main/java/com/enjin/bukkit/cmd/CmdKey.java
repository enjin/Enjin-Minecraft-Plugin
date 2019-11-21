package com.enjin.bukkit.cmd;

import com.enjin.bukkit.EnjinMinecraftPlugin;
import com.enjin.bukkit.i18n.Translation;
import com.enjin.core.Enjin;
import com.enjin.core.EnjinServices;
import com.enjin.core.config.EnjinConfig;
import com.enjin.rpc.mappings.mappings.general.RPCData;
import com.enjin.rpc.mappings.mappings.plugin.Auth;
import com.enjin.rpc.mappings.services.PluginService;
import com.google.common.base.Optional;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

import static com.enjin.bukkit.enums.Permission.CMD_KEY;

public class CmdKey extends EnjinCommand {

    public CmdKey(EnjinCommand parent) {
        super(parent);
        init(parent.plugin);
    }

    private void init(EnjinMinecraftPlugin plugin) {
        this.aliases.add("key");
        this.aliases.add("setkey");
        this.aliases.add("enjinkey");
        this.aliases.add("ek");

        this.requiredArgs.add("key");
        this.requirements = CommandRequirements.builder(plugin)
                .withPermission(CMD_KEY)
                .build();

        plugin.getCommand("enjinkey").setExecutor(this);
    }

    @Override
    public void execute(CommandContext context) {
        String key = context.args.get(0);
        EnjinConfig conf = Enjin.getConfiguration();

        if (conf.getAuthKey().equals(key)) {
            Translation.Command_Key_AlreadyAuthenticated.send(context.sender);
            return;
        }

        Translation.Command_Key_CheckingValidity.send(context.sender);
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> checkKeyValidity(context.sender, key));
    }

    @Override
    public Translation getUsageTranslation() {
        return Translation.Command_Key_Description;
    }

    private void checkKeyValidity(CommandSender sender, String key) {
        PluginService service = EnjinServices.getService(PluginService.class);
        RPCData<Auth> data = service.auth(Optional.of(key), Bukkit.getPort(), true, true);

        if (data == null) {
            Translation.Errors_Network_Connection.send(sender);
            return;
        }

        if (data.getError() != null) {
            Translation.Errors_Error.send(sender, data.getError().getMessage());
            return;
        }

        if (data.getResult() != null && data.getResult().isAuthed()) {
            Translation.Command_Key_SuccessfulValidation.send(sender);
            Enjin.getConfiguration().setAuthKey(key);
            plugin.setAuthKeyInvalid(false);
            plugin.init();
        } else {
            Translation.Command_Key_UnsuccessfulValidation.send(sender);
        }
    }
}
