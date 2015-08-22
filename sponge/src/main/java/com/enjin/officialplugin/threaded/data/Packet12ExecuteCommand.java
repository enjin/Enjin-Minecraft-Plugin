package com.enjin.officialplugin.threaded.data;

import com.enjin.officialplugin.EnjinMinecraftPlugin;
import com.enjin.officialplugin.utils.commands.CommandWrapper;
import com.enjin.officialplugin.utils.packet.PacketUtilities;
import org.spongepowered.api.util.command.CommandSource;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Packet12ExecuteCommand {
    private static Pattern idregex = Pattern.compile("^(\\d+):(.*)");

    public static void handle(BufferedInputStream in, EnjinMinecraftPlugin plugin) {
        try {
            String input = PacketUtilities.readString(in);

            if (input == null || input.isEmpty()) {
                EnjinMinecraftPlugin.getInstance().debug("Input is null or empty. Input: " + input);
                return;
            }

            String[] inputs = input.split("\0");
            final String info = inputs[0];
            final Matcher matcher = idregex.matcher(info);
            final String id;
            final String command;
            long delay = 0;

            if (matcher.matches()) {
                id = matcher.group(1);
                command = matcher.group(2);
            } else {
                EnjinMinecraftPlugin.getInstance().debug("Input does not match id:command format. Input: " + input);
                return;
            }

            if (id.isEmpty()) {
                EnjinMinecraftPlugin.getInstance().debug("Invalid or missing command id. Input: " + input);
                return;
            }

            if (inputs.length > 1) {
                try {
                    delay = Long.parseLong(inputs[1]);
                } catch (NumberFormatException e) {
                    delay = 0;
                }
            }

            final CommandSource console = EnjinMinecraftPlugin.getInstance().getGame().getServer().getConsole();
            EnjinMinecraftPlugin.getInstance().addProcessedCommand(new CommandWrapper(null, command, id));

            if (delay > 0) {
                EnjinMinecraftPlugin.getInstance().debug("Running command \"" + command + "\" in " + delay + " seconds.");
                EnjinMinecraftPlugin.getInstance().getGame().getScheduler().createTaskBuilder()
                        .execute(new Runnable() {
                            @Override
                            public void run() {
                                EnjinMinecraftPlugin.getInstance().getGame().getCommandDispatcher().process(console, command);
                            }
                        })
                        .delay(delay, TimeUnit.SECONDS)
                        .submit(EnjinMinecraftPlugin.getInstance());
            } else {
                EnjinMinecraftPlugin.getInstance().debug("Running command \"" + command + "\".");
                EnjinMinecraftPlugin.getInstance().getGame().getCommandDispatcher().process(console, command);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
