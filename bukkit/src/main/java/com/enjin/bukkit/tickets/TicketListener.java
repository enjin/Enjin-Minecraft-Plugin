package com.enjin.bukkit.tickets;

import com.enjin.bukkit.EnjinMinecraftPlugin;
import com.enjin.bukkit.modules.impl.SupportModule;
import com.enjin.rpc.mappings.mappings.tickets.TicketModule;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;

import java.util.Map;

public class TicketListener implements Listener {
    @EventHandler
    public void onPlayerCommandPreprocess(final PlayerCommandPreprocessEvent event) {
        String[] args = event.getMessage().split(" ");
        if (args.length == 1) {
            String command = args[0].replace("/", "");
			SupportModule module = EnjinMinecraftPlugin.getInstance().getModuleManager().getModule(SupportModule.class);
			if (module != null) {
				for (final Map.Entry<Integer, TicketModule> entry : module.getModules().entrySet()) {
					final TicketModule m = entry.getValue();
					if (m.getCommand() != null && !m.getCommand().isEmpty()) {
						if (m.getCommand().equalsIgnoreCase(command)) {
							event.setCancelled(true);

							if (TicketCreationSession.getSessions().containsKey(event.getPlayer().getUniqueId())) {
								event.getPlayer().sendMessage(ChatColor.RED + "A ticket session is already in progress...");
								return;
							}

							Bukkit.getScheduler().runTaskAsynchronously(EnjinMinecraftPlugin.getInstance(), new Runnable() {
								@Override
								public void run() {
									new TicketCreationSession(event.getPlayer(), entry.getKey(), m);
								}
							});
						}
					}
				}
			}
        }
    }
}
