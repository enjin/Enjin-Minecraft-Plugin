package com.enjin.bukkit.cmd;

import com.enjin.bukkit.config.EMPConfig;
import com.enjin.bukkit.i18n.Translation;
import com.enjin.core.Enjin;

import static com.enjin.bukkit.enums.Permission.CMD_DEBUG;

public class CmdDebug extends EnjinCommand {

    public CmdDebug(EnjinCommand parent) {
        super(parent.plugin, parent);
        this.aliases.add("debug");
        this.requirements = CommandRequirements.builder(parent.plugin)
                .withPermission(CMD_DEBUG)
                .build();
    }

    @Override
    public void execute(CommandContext context) {
        EMPConfig config = Enjin.getConfiguration(EMPConfig.class);
        config.setDebug(!config.isDebug());
        Translation.Command_Debug_Set.send(context.sender, config.isDebug());
    }

    @Override
    public Translation getUsageTranslation() {
        return Translation.Command_Debug_Description;
    }
}
