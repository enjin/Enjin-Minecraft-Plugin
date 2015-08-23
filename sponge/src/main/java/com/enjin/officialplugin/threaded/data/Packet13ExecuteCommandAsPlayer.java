package com.enjin.officialplugin.threaded.data;

import com.enjin.officialplugin.EnjinMinecraftPlugin;
import com.enjin.officialplugin.utils.packet.PacketUtilities;
import com.google.common.base.Optional;
import org.spongepowered.api.entity.player.Player;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Packet13ExecuteCommandAsPlayer {
    static Pattern idregex = Pattern.compile("^(\\d+):(.+)");

    public static void handle(BufferedInputStream in, EnjinMinecraftPlugin plugin) {
        try {
            String name = PacketUtilities.readString(in);
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

            final Optional<Player> optional = EnjinMinecraftPlugin.getInstance().getGame().getServer().getPlayer(name);
            if (optional.isPresent()) {
                if (delay > 0) {
                    EnjinMinecraftPlugin.getInstance().debug("Running command \"" + command + "\" in " + delay + " seconds.");
                    EnjinMinecraftPlugin.getInstance().getGame().getScheduler().createTaskBuilder()
                            .execute(new Runnable() {
                                @Override
                                public void run() {
                                    EnjinMinecraftPlugin.getInstance().getGame().getCommandDispatcher().process(optional.get(), command);
                                }
                            })
                            .delay(delay, TimeUnit.SECONDS)
                            .submit(EnjinMinecraftPlugin.getInstance());
                } else {
                    EnjinMinecraftPlugin.getInstance().debug("Running command \"" + command + "\".");
                    EnjinMinecraftPlugin.getInstance().getGame().getCommandDispatcher().process(optional.get(), command);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
