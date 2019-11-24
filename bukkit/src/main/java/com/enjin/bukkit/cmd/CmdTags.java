package com.enjin.bukkit.cmd;

import com.enjin.bukkit.cmd.arg.PlayerArgumentProcessor;
import com.enjin.bukkit.i18n.Translation;
import com.enjin.bukkit.util.text.MessageUtil;
import com.enjin.bukkit.util.text.TextBuilder;
import com.enjin.bukkit.util.text.TextUtils;
import com.enjin.core.EnjinServices;
import com.enjin.rpc.mappings.mappings.general.RPCData;
import com.enjin.rpc.mappings.mappings.plugin.TagData;
import com.enjin.rpc.mappings.services.PluginService;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Iterator;
import java.util.List;
import java.util.Optional;

import static com.enjin.bukkit.enums.Permission.CMD_TAGS;

public class CmdTags extends EnjinCommand {

    public CmdTags(EnjinCommand parent) {
        super(parent.plugin, parent);
        this.aliases.add("tags");
        this.requiredArgs.add("player");
        this.requirements = CommandRequirements.builder(parent.plugin)
                .withPermission(CMD_TAGS)
                .requireValidKey()
                .build();
    }

    @Override
    public void execute(CommandContext context) {
        new GetTags(context).runTaskAsynchronously(plugin);
    }

    @Override
    public Translation getUsageTranslation() {
        return Translation.Command_Tags_Description;
    }

    class GetTags extends BukkitRunnable {

        private CommandContext context;
        private String name;

        public GetTags(CommandContext context) {
            this.context = context;
            this.name = context.args.get(0);
            this.name = name.substring(0, name.length() > 16 ? 16 : name.length());
        }

        @Override
        public void run() {
            PluginService service = EnjinServices.getService(PluginService.class);
            RPCData<List<TagData>> data = service.getTags(name);

            if (data == null) {
                Translation.Errors_Network_Connection.send(context);
                return;
            }

            List<TagData> tags = data.getResult();
            if (tags.isEmpty()) {
                Translation.Command_Tags_Empty.send(context, context.sender.getName());
                return;
            }

            TextBuilder text = new TextBuilder();
            Iterator<TagData> it = tags.iterator();
            while (it.hasNext()) {
                if (!text.isEmpty())
                    text.append(ChatColor.GOLD).append(", ");
                TagData tag = it.next();
                text.append(ChatColor.GREEN).append(tag.getName());
            }

            Translation.Command_Tags_List.send(context, context.sender.getName(), text.toString());
        }

    }
}
