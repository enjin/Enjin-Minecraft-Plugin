package com.enjin.officialplugin.threaded.data;

import com.enjin.officialplugin.EnjinMinecraftPlugin;
import com.enjin.officialplugin.utils.packet.PacketUtilities;
import org.spongepowered.api.util.command.CommandSource;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

public class Packet12ExecuteCommand {
    private static Pattern idregex = Pattern.compile("^(\\d+):(.*)");

    public static void handle(BufferedInputStream in, EnjinMinecraftPlugin plugin) {
        try {
            String input = PacketUtilities.readString(in);

            if (input == null || input.isEmpty() || !input.startsWith("/")) {
                return;
            }

            String[] inputs = input.split("\0");
            final String command = inputs[0];
            long delay = 0;

            if (inputs.length > 1) {
                try {
                    delay = Long.parseLong(inputs[1]);
                } catch (NumberFormatException e) {
                    delay = 0;
                }
            }

            final CommandSource console = EnjinMinecraftPlugin.getInstance().getGame().getServer().getConsole();

            String debug = "Running command \"" + command + "\"";
            if (delay > 0) {
                EnjinMinecraftPlugin.getInstance().debug(debug + " in " + delay + " seconds.");
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
                EnjinMinecraftPlugin.getInstance().debug(debug + ".");
                EnjinMinecraftPlugin.getInstance().getGame().getCommandDispatcher().process(console, command);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
