package com.enjin.bukkit.command.commands;

import com.enjin.bukkit.EnjinMinecraftPlugin;
import com.enjin.bukkit.command.Directive;
import com.enjin.bukkit.command.Permission;
import com.enjin.bukkit.tickets.TicketCreationSession;
import com.enjin.bukkit.tickets.TicketViewBuilder;
import com.enjin.core.EnjinServices;
import com.enjin.rpc.mappings.mappings.general.RPCData;
import com.enjin.rpc.mappings.mappings.tickets.Module;
import com.enjin.rpc.mappings.mappings.tickets.Reply;
import com.enjin.rpc.mappings.mappings.tickets.Ticket;
import com.enjin.rpc.mappings.services.TicketService;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Map;

public class SupportCommands {
    @Permission(value = "enjin.support")
    @Directive(parent = "enjin", value = "support")
    public static void support(Player sender, String[] args) {
        EnjinMinecraftPlugin plugin = EnjinMinecraftPlugin.instance;
        Map<Integer, Module> modules = EnjinMinecraftPlugin.getModules();

        if (plugin.getAuthKey() == null || plugin.getAuthKey().isEmpty()) {
            sender.sendMessage("Cannot use this value without setting your key.");
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

    @Permission(value = "enjin.ticket")
    @Directive(parent = "enjin", value = "ticket")
    public static void ticket(Player sender, String[] args) {
        EnjinMinecraftPlugin plugin = EnjinMinecraftPlugin.instance;

        if (plugin.getAuthKey() == null || plugin.getAuthKey().isEmpty()) {
            sender.sendMessage("Cannot use this value without setting your key.");
            return;
        }

        if (!(sender instanceof Player)) {
            sender.sendMessage("Only players can view their own tickets.");
            return;
        }

        if (args.length == 1) {
            final Player player = sender;
            Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                TicketService service = EnjinServices.getService(TicketService.class);
                RPCData<List<Ticket>> data = service.getPlayerTickets(plugin.getAuthKey(), -1, player.getName());

                if (data != null) {
                    if (data.getError() != null) {
                        player.sendMessage(data.getError().getMessage());
                    } else {
                        List<Ticket> tickets = data.getResult();
                        if (tickets.size() > 0) {
                            player.spigot().sendMessage(TicketViewBuilder.buildTicketList(tickets));
                        } else {
                            player.sendMessage("You do not have any tickets at this time!");
                        }
                    }
                } else {
                    player.sendMessage("Could not fetch your tickets.");
                }
            });
        } else {
            final Player player = sender;
            Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                TicketService service = EnjinServices.getService(TicketService.class);
                RPCData<List<Reply>> data = service.getReplies(plugin.getAuthKey(), -1, args[1], player.getName());

                if (data != null) {
                    if (data.getError() != null) {
                        player.sendMessage(data.getError().getMessage());
                    } else {
                        List<Reply> replies = data.getResult();
                        if (replies.size() > 0) {
                            player.spigot().sendMessage(TicketViewBuilder.buildTicket(args[1], replies, player.hasPermission("enjin.ticket.private")));
                        } else {
                            player.sendMessage("You entered an invalid ticket code!");
                        }
                    }
                } else {
                    player.sendMessage("Could not fetch ticket replies.");
                }
            });
        }
    }
}
