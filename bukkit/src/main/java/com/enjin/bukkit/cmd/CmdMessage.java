package com.enjin.bukkit.cmd;

import com.enjin.bukkit.cmd.arg.PlayerArgumentProcessor;
import com.enjin.bukkit.i18n.Translation;
import com.enjin.bukkit.util.text.MessageUtil;
import com.enjin.bukkit.util.text.TextUtils;
import org.bukkit.entity.Player;

import java.util.Optional;

import static com.enjin.bukkit.enums.Permission.CMD_MESSAGE;

public class CmdMessage extends EnjinCommand {

    public CmdMessage(EnjinCommand parent) {
        super(parent.plugin, parent);
        this.aliases.add("message");
        this.aliases.add("msg");
        this.aliases.add("inform");
        this.requiredArgs.add("player");
        this.requiredArgs.add("message");
        this.requirements = CommandRequirements.builder(parent.plugin)
                .withPermission(CMD_MESSAGE)
                .build();
    }

    @Override
    public void execute(CommandContext context) {
        Optional<Player> optionalPlayer = PlayerArgumentProcessor.INSTANCE.parse(context.sender, context.args.get(0));

        if (!optionalPlayer.isPresent()) {
            Translation.Player_NotOnline.send(context.sender, context.args.get(0));
            return;
        }

        String message = TextUtils.concat(context.args, " ", 1);
        MessageUtil.sendString(optionalPlayer.get(), message);
    }

    @Override
    public Translation getUsageTranslation() {
        return Translation.Command_Message_Description;
    }
}
