package com.enjin.officialplugin.sync.data;

import java.io.BufferedInputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.enjin.core.Enjin;
import com.enjin.officialplugin.util.PacketUtilities;
import com.enjin.rpc.mappings.mappings.plugin.data.ExecuteData;
import org.bukkit.Bukkit;

import com.enjin.officialplugin.CommandWrapper;
import com.enjin.officialplugin.EnjinMinecraftPlugin;

/**
 * @author OverCaste (Enjin LTE PTD).
 *         This software is released under an Open Source license.
 * @copyright Enjin 2012.
 */

public class Packet12ExecuteCommand {
    static Pattern idregex = Pattern.compile("^(\\d+):(.*)");

    public static void handle(BufferedInputStream in, EnjinMinecraftPlugin plugin) {
        try {
            String command = PacketUtilities.readString(in);
            Matcher commandmatcher = idregex.matcher(command);
            String commandid = "";
            if (commandmatcher.matches()) {
                commandid = commandmatcher.group(1);
                command = commandmatcher.group(2);
            }
            if (command.equals("")) {
                Enjin.getPlugin().debug("Got a blank command from enjin!");
                CommandWrapper executedcommand = new CommandWrapper(Bukkit.getConsoleSender(), command, commandid);
                plugin.addCommandID(executedcommand);
                return;
            }
            String[] commandsplit = command.split("\0");
            CommandWrapper executedcommand;
            if (commandsplit.length > 1) {
                try {
                    long time = System.currentTimeMillis() + (Long.parseLong(commandsplit[1]) * 1000);
                    Enjin.getPlugin().debug("Executing command \"" + command + "\" as console in " + commandsplit[1] + " seconds.");
                    executedcommand = new CommandWrapper(Bukkit.getConsoleSender(), commandsplit[0], time, commandid);
                    plugin.commexecuter.addCommand(executedcommand);
                } catch (NumberFormatException e) {
                    Enjin.getPlugin().debug("Failed to get the time on a timed command, adding as a regular command");
                    executedcommand = new CommandWrapper(Bukkit.getConsoleSender(), commandsplit[0], commandid);
                    plugin.commandqueue.addCommand(executedcommand);
                }
            } else {
                Enjin.getPlugin().debug("Executing command \"" + command + "\" as console.");
                executedcommand = new CommandWrapper(Bukkit.getConsoleSender(), command, commandid);
                plugin.commandqueue.addCommand(executedcommand);
            }
            //TODO: add in code for executed commands.
            plugin.addCommandID(executedcommand);
        } catch (Throwable t) {
            Bukkit.getLogger().warning("Failed to dispatch command via 0x12, " + t.getMessage());
            t.printStackTrace();
        }
    }

    public static void handle(ExecuteData data) {
        Enjin.getPlugin().getInstructionHandler().execute(data.getId(), data.getCommand(), data.getDelay());
    }
}
