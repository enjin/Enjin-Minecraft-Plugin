package com.enjin.bukkit.command;

import com.enjin.core.Enjin;
import com.google.common.base.Optional;
import org.bukkit.command.*;

public class DispatchCommand extends org.bukkit.command.Command {
    protected DispatchCommand() {
        super("dispatcher");
    }

    @Override
    public boolean execute(CommandSender sender, String label, String[] args) {
        Enjin.getLogger().debug("Executing registered command: " + label);
        if (CommandBank.nodes.size() == 0) {
            return false;
        }

        Optional<CommandNode> w = Optional.fromNullable(CommandBank.nodes.get(label));
        if (w.isPresent()) {
            CommandNode wrapper = w.get();
            wrapper.invoke(sender, args);
            return true;
        }

        return false;
    }
}
