package com.enjin.bukkit.cmd;

import com.enjin.bukkit.EnjinMinecraftPlugin;
import com.enjin.bukkit.enums.Usage;
import com.enjin.bukkit.i18n.Translation;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.PluginDescriptionFile;

public class CmdEnjin extends EnjinCommand {

    private CmdHelp cmdHelp;
    private CmdBuy cmdBuy;

    public CmdEnjin(EnjinMinecraftPlugin plugin) {
        super(plugin);

        aliases.add("enjin");
        aliases.add("e");

        cmdHelp = new CmdHelp(this);
        cmdBuy = new CmdBuy(this);
        addSubCommand(new CmdBroadcast(this));
        addSubCommand(cmdBuy);
        addSubCommand(new CmdDebug(this));
        addSubCommand(cmdHelp);
        addSubCommand(new CmdKey(this));
        addSubCommand(new CmdLag(this));
        addSubCommand(new CmdMessage(this));
        addSubCommand(new CmdPush(this));
        addSubCommand(new CmdReport(this));
        addSubCommand(new CmdTags(this));

        PluginCommand command = plugin.getCommand("enjin");
        if (command == null)
            throw new IllegalStateException("Could not get plugin command: \"enjin\"");
        command.setExecutor(this);
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
