package com.enjin.bukkit.cmd;

import com.enjin.bukkit.config.EMPConfig;
import com.enjin.bukkit.i18n.Translation;
import com.enjin.core.Enjin;

import java.util.ArrayList;

import static com.enjin.bukkit.enums.Permission.CMD_BUY;

public class CmdBuy extends EnjinCommand {

    public CmdBuy(EnjinCommand parent) {
        super(parent.plugin, parent);
        String command = Enjin.getConfiguration(EMPConfig.class).getBuyCommand();
        if (command != null)
            this.aliases.add(command);
        this.aliases.add("buy");
        this.requirements = CommandRequirements.builder(parent.plugin)
                .withAllowedSenderTypes(SenderType.PLAYER)
                .withPermission(CMD_BUY)
                .requireValidKey()
                .build();
        register(this.aliases.get(0), new ArrayList<>(0));
    }

    @Override
    public void execute(CommandContext context) {
        context.sender.sendMessage("test");
    }

    @Override
    public Translation getUsageTranslation() {
        return Translation.Command_Buy_Description;
    }
}
