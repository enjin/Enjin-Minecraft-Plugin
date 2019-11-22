package com.enjin.bukkit.cmd;

import com.enjin.bukkit.i18n.Translation;
import com.enjin.bukkit.tasks.ReportPublisher;

import static com.enjin.bukkit.enums.Permission.CMD_REPORT;

public class CmdReport extends EnjinCommand {

    public CmdReport(EnjinCommand parent) {
        super(parent.plugin, parent);
        aliases.add("report");
        requirements = CommandRequirements.builder(parent.plugin)
                .withPermission(CMD_REPORT)
                .build();
    }

    @Override
    public void execute(CommandContext context) {
        Translation.Command_Report_Generating.send(context);
        new ReportPublisher(plugin, context.sender).runTaskAsynchronously(plugin);
    }

    @Override
    public Translation getUsageTranslation() {
        return Translation.Command_Report_Description;
    }
}
