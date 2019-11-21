package com.enjin.bukkit.cmd;

import com.enjin.bukkit.i18n.Translation;
import com.enjin.bukkit.util.text.TextUtils;
import org.bukkit.Bukkit;

import static com.enjin.bukkit.enums.Permission.CMD_BROADCAST;

public class CmdBroadcast extends EnjinCommand {

    public CmdBroadcast(EnjinCommand parent) {
        super(parent.plugin, parent);
        this.aliases.add("broadcast");
        this.aliases.add("announce");
        this.aliases.add("say");
        this.requiredArgs.add("message");
        this.requirements = CommandRequirements.builder(parent.plugin)
                .withPermission(CMD_BROADCAST)
                .build();
    }

    @Override
    public void execute(CommandContext context) {
        String message = TextUtils.concat(context.args, " ");
        Bukkit.broadcastMessage(TextUtils.colorize(message));
    }

    @Override
    public Translation getUsageTranslation() {
        return Translation.Command_Broadcast_Description;
    }
}
