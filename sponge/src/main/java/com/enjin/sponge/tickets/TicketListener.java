package com.enjin.sponge.tickets;

import com.enjin.core.Enjin;
import com.enjin.rpc.mappings.mappings.tickets.Module;
import com.enjin.sponge.EnjinMinecraftPlugin;
import com.enjin.sponge.managers.TicketManager;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.Order;
import org.spongepowered.api.event.command.SendCommandEvent;
import org.spongepowered.api.event.filter.cause.Root;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import java.util.Map;

public class TicketListener {
    @Listener
    public void onSendCommand(final SendCommandEvent event, @Root Player player) {
        String[] args = (event.getCommand() + " " + event.getArguments()).split(" ");
        if (args.length == 1) {
            String command = args[0].replace("/", "");
            for (final Map.Entry<Integer, Module> entry : TicketManager.getModules().entrySet()) {
                final Module module = entry.getValue();
                if (module.getCommand() != null && !module.getCommand().isEmpty()) {
                    if (module.getCommand().equalsIgnoreCase(command)) {
						Enjin.getLogger().debug("Running support command: " + command);
						event.setCancelled(true);

                        if (TicketCreationSession.getSessions().containsKey(player.getUniqueId())) {
                            player.sendMessage(Text.of(TextColors.RED, "A ticket session is already in progress..."));
                            return;
                        }

						EnjinMinecraftPlugin.getInstance().getAsync().execute(() -> new TicketCreationSession(player, entry.getKey(), module));
                    }
                }
            }
        }
    }
}
