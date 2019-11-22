package com.enjin.bukkit.cmd;

import com.enjin.bukkit.i18n.Translation;
import com.enjin.bukkit.listeners.ConnectionListener;
import org.bukkit.Bukkit;

import static com.enjin.bukkit.enums.Permission.CMD_PUSH;

public class CmdPush extends EnjinCommand {

    public CmdPush(EnjinCommand parent) {
        super(parent.plugin, parent);
        this.aliases.add("push");
        this.requirements = CommandRequirements.builder(parent.plugin)
                .withPermission(CMD_PUSH)
                .requireValidKey()
                .build();
    }

    @Override
    public void execute(CommandContext context) {
        ConnectionListener.updatePlayersRanks(Bukkit.getOfflinePlayers());
        Translation.Command_Push_Updating.send(context.sender);
    }

    @Override
    public Translation getUsageTranslation() {
        return Translation.Command_Push_Description;
    }
}
