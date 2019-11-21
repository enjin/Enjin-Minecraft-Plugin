package com.enjin.bukkit.cmd;

import com.enjin.bukkit.enums.Permission;
import com.enjin.bukkit.i18n.Translation;
import org.bukkit.command.CommandSender;

public class CmdHelp extends EnjinCommand {

    public CmdHelp(EnjinCommand parent) {
        super(parent.plugin, parent);
        this.aliases.add("help");
        this.aliases.add("h");
        this.requirements = CommandRequirements.builder(parent.plugin)
                .withAllowedSenderTypes(SenderType.ANY)
                .withPermission(Permission.CMD_HELP)
                .build();
    }

    @Override
    public void execute(CommandContext context) {
        parent.ifPresent(parent -> showHelp(context.sender, parent));
    }

    private void showHelp(CommandSender sender, EnjinCommand command) {
        command.showHelp(sender);
        command.subCommands.forEach(c -> showHelp(sender, c));
    }

    @Override
    public Translation getUsageTranslation() {
        return Translation.Command_Help_Description;
    }

}
