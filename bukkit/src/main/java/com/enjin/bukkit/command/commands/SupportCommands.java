package com.enjin.bukkit.command.commands;

import com.enjin.bukkit.EnjinMinecraftPlugin;
import com.enjin.bukkit.command.Directive;
import com.enjin.bukkit.command.Permission;
import com.enjin.bukkit.tickets.TicketCreationSession;
import com.enjin.core.Enjin;
import com.enjin.rpc.mappings.mappings.tickets.Module;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.Map;

public class SupportCommands {
    @Permission(value = "enjin.support")
    @Directive(parent = "enjin", directive = "support")
    public static void support(Player sender, String[] args) {
        EnjinMinecraftPlugin plugin = EnjinMinecraftPlugin.instance;
        Map<Integer, Module> modules = EnjinMinecraftPlugin.getModules();

        if (plugin.getAuthKey() == null) {
            sender.sendMessage("Cannot use this command without setting your key.");
            return;
        }

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            plugin.pollModules();

            if (modules.size() == 0) {
                sender.sendMessage("Support tickets are not available on this server.");
                return;
            }

            if (args.length > 1) {
                int moduleId;

                try {
                    moduleId = Integer.parseInt(args[1]);
                } catch (NumberFormatException e) {
                    sender.sendMessage("You must enter the numeric id of the module!");
                    return;
                }

                plugin.debug("Checking if module with id \"" + moduleId + "\" exists.");
                final Module module = modules.get(moduleId);
                if (module != null) {
                    new TicketCreationSession(sender, moduleId, module);
                } else {
                    sender.sendMessage("No module with id \"" + moduleId + "\" exists.");
                    plugin.debug("Existing modules:");
                    for (Integer id : modules.keySet()) {
                        plugin.debug(String.valueOf(id));
                    }
                }
            } else {
                if (modules.size() == 1) {
                    final Map.Entry<Integer, Module> entry = modules.entrySet().iterator().next();
                    Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> new TicketCreationSession(sender, entry.getKey(), entry.getValue()));
                } else {
                    plugin.debug(String.valueOf(modules.size()));
                    for (Map.Entry<Integer, Module> entry : modules.entrySet()) {
                        int id = entry.getKey();
                        Module module = entry.getValue();
                        sender.sendMessage(ChatColor.translateAlternateColorCodes('&', (module.getHelp() != null && !module.getHelp().isEmpty()) ? module.getHelp() : "Type /e support " + id + " to create a support ticket for " + module.getName().replaceAll("\\s+", " ")));
                    }
                }
            }
        });
    }
}
