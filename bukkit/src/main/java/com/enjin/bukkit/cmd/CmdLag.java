package com.enjin.bukkit.cmd;

import com.enjin.bukkit.i18n.Translation;
import com.enjin.bukkit.tasks.TPSMonitor;

import static com.enjin.bukkit.enums.Permission.CMD_LAG;

public class CmdLag extends EnjinCommand {

    public CmdLag(EnjinCommand parent) {
        super(parent.plugin, parent);
        this.aliases.add("lag");
        this.requirements = CommandRequirements.builder(parent.plugin)
                .withPermission(CMD_LAG)
                .build();
    }

    @Override
    public void execute(CommandContext context) {
        TPSMonitor monitor = TPSMonitor.getInstance();
        String averageTps = TPSMonitor.getDecimalFormat().format(monitor.getTPSAverage());
        String lastTps = TPSMonitor.getDecimalFormat().format(monitor.getLastTPSMeasurement());
        Runtime runtime = Runtime.getRuntime();
        long curMem = (runtime.maxMemory() - runtime.freeMemory()) / (1024 * 1024);
        long maxMem = runtime.maxMemory() / (1024 * 1024);

        Translation.Command_Lag_AverageTps.send(context.sender, averageTps);
        Translation.Command_Lag_LastTps.send(context.sender, lastTps);
        Translation.Command_Lag_MemoryUsed.send(context.sender, curMem, maxMem);
    }

    @Override
    public Translation getUsageTranslation() {
        return Translation.Command_Lag_Description;
    }
}
