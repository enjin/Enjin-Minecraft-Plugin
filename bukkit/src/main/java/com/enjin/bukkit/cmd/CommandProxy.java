package com.enjin.bukkit.cmd;

import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginIdentifiableCommand;
import org.bukkit.plugin.Plugin;

import java.util.List;

public class CommandProxy extends Command implements PluginIdentifiableCommand {

    private Plugin plugin;
    private EnjinCommand command;

    protected CommandProxy(EnjinCommand command, String name, List<String> aliases) {
        super(name, command.getUsageTranslation().defaultTranslation(), "", aliases);
        this.plugin = command.plugin;
        this.command = command;
    }

    protected CommandProxy(EnjinCommand command, String name) {
        super(name);
        this.plugin = command.plugin;
        this.command = command;
    }

    @Override
    public boolean execute(CommandSender sender, String alias, String[] args) {
        return command.onCommand(sender, this, alias, args);
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String alias, String[] args) {
        return command.onTabComplete(sender, this,  alias, args);
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String alias, String[] args, Location location) {
        return command.onTabComplete(sender, this, alias, args);
    }

    @Override
    public Plugin getPlugin() {
        return plugin;
    }

}
