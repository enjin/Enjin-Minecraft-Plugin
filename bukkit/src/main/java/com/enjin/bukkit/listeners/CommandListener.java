package com.enjin.bukkit.listeners;

import com.enjin.bukkit.EnjinMinecraftPlugin;
import com.enjin.bukkit.commands.store.BuyCommand;
import com.enjin.core.Enjin;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import java.util.Arrays;

public class CommandListener implements Listener {
    @EventHandler(priority = EventPriority.LOWEST)
    public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event) {
        if (!event.getMessage().startsWith("/")) {
            Enjin.getPlugin().debug("Command does not start with /. Skipping.");
            return;
        }

        String[] parts = event.getMessage().split(" ");

        if (parts.length == 0) {
            Enjin.getPlugin().debug("Command is empty. Skipping.");
            return;
        }

        String command = parts[0].replaceFirst("/", "");
        String[] args = parts.length > 1 ? Arrays.copyOfRange(parts, 1, parts.length) : new String[]{};

        if (command.equalsIgnoreCase("buy") || command.equalsIgnoreCase(EnjinMinecraftPlugin.config.getBuyCommand())) {
            BuyCommand.buy(event.getPlayer(), args);
            event.setCancelled(true);
        }
    }
}
