package com.enjin.bukkit.cmd;

import com.enjin.bukkit.EnjinMinecraftPlugin;
import com.enjin.bukkit.enums.Usage;
import com.enjin.bukkit.i18n.Translation;
import org.bukkit.plugin.PluginDescriptionFile;

public class CmdEnjin extends EnjinCommand {

    private CmdHelp cmdHelp;

    public CmdEnjin(EnjinMinecraftPlugin plugin) {
        super(plugin);
        cmdHelp = new CmdHelp(this);

        aliases.add("enjin");
        aliases.add("e");

        addSubCommand(new CmdBroadcast(this));
        addSubCommand(new CmdDebug(this));
        addSubCommand(new CmdKey(this));
        addSubCommand(new CmdMessage(this));
        addSubCommand(cmdHelp);

        plugin.getCommand("enjin").setExecutor(this);
    }

    @Override
    public void execute(CommandContext context) {
        PluginDescriptionFile description = plugin.getDescription();
        Translation.Command_Enjin_Info.send(context.sender,
                description.getName(),
                description.getVersion(),
                cmdHelp.getUsage(context.senderType, Usage.COMMAND_ONLY));
    }

    @Override
    public Translation getUsageTranslation() {
        return Translation.Command_Enjin_Description;
    }

}
