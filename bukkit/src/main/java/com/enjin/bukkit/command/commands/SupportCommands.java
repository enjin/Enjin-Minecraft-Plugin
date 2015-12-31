package com.enjin.bukkit.command.commands;

import com.enjin.bukkit.EnjinMinecraftPlugin;
import com.enjin.bukkit.command.Directive;
import com.enjin.bukkit.command.Permission;
import com.enjin.bukkit.managers.TicketManager;
import com.enjin.bukkit.tickets.TicketCreationSession;
import com.enjin.bukkit.tickets.TicketViewBuilder;
import com.enjin.core.EnjinServices;
import com.enjin.rpc.mappings.mappings.general.RPCData;
import com.enjin.rpc.mappings.mappings.general.RPCSuccess;
import com.enjin.rpc.mappings.mappings.tickets.*;
import com.enjin.rpc.mappings.services.TicketService;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class SupportCommands {
    @Permission(value = "enjin.support")
    @Directive(parent = "enjin", value = "support", requireValidKey = true)
    public static void support(final Player sender, final String[] args) {
        final EnjinMinecraftPlugin plugin = EnjinMinecraftPlugin.getInstance();
        final Map<Integer, Module> modules = TicketManager.getModules();

        if (TicketCreationSession.getSessions().containsKey(sender.getUniqueId())) {
            sender.sendMessage(ChatColor.RED + "A ticket session is already in progress...");
            return;
        }

        Bukkit.getScheduler().runTaskAsynchronously(plugin, new Runnable() {
            @Override
            public void run() {
                TicketManager.pollModules();

                if (modules.size() == 0) {
                    sender.sendMessage("Support tickets are not available on this server.");
                    return;
                }

                if (args.length > 0) {
                    int moduleId;

                    try {
                        moduleId = Integer.parseInt(args[0]);
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
                        Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
                            @Override
                            public void run() {
                                new TicketCreationSession(sender, entry.getKey(), entry.getValue());
                            }
                        });
                    } else {
                        plugin.debug(String.valueOf(modules.size()));
                        for (Map.Entry<Integer, Module> entry : modules.entrySet()) {
                            int id = entry.getKey();
                            Module module = entry.getValue();
                            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', (module.getHelp() != null && !module.getHelp().isEmpty()) ? module.getHelp() : "Type /e support " + id + " to create a support ticket for " + module.getName().replaceAll("\\s+", " ")));
                        }
                    }
                }
            }
        });
    }

    @Permission(value = "enjin.ticket")
    @Directive(parent = "enjin", value = "ticket", aliases = "tickets", requireValidKey = true)
    public static void ticket(final Player sender, final String[] args) {
        EnjinMinecraftPlugin plugin = EnjinMinecraftPlugin.getInstance();

        if (args.length == 0) {
            final Player player = sender;
            Bukkit.getScheduler().runTaskAsynchronously(plugin, new Runnable() {
                @Override
                public void run() {
                    TicketService service = EnjinServices.getService(TicketService.class);
                    RPCData<TicketResults> data = service.getPlayerTickets(-1, player.getName());

                    if (data != null) {
                        if (data.getError() != null) {
                            player.sendMessage(data.getError().getMessage());
                        } else {
                            List<Ticket> tickets = data.getResult().getResults();
                            if (tickets.size() > 0) {
                                TicketViewBuilder.buildTicketList(tickets).send(player);
                            } else {
                                player.sendMessage("You do not have any tickets at this time!");
                            }
                        }
                    } else {
                        player.sendMessage("Could not fetch your tickets.");
                    }
                }
            });
        } else {
            final Player player = sender;
            Bukkit.getScheduler().runTaskAsynchronously(plugin, new Runnable() {
                @Override
                public void run() {
                    TicketService service = EnjinServices.getService(TicketService.class);
                    RPCData<ReplyResults> data = service.getReplies(-1, args[0], player.getName());

                    if (data != null) {
                        if (data.getError() != null) {
                            player.sendMessage(data.getError().getMessage());
                        } else {
                            List<Reply> replies = data.getResult().getResults();
                            if (replies.size() > 0) {
                                TicketViewBuilder.buildTicket(args[0], replies, player.hasPermission("enjin.ticket.private")).send(player);
                            } else {
                                player.sendMessage("You entered an invalid ticket code!");
                            }
                        }
                    } else {
                        player.sendMessage("Could not fetch ticket replies.");
                    }
                }
            });
        }
    }

    @Permission(value = "enjin.ticket.open")
    @Directive(parent = "enjin", value = "openticket", aliases = "opentickets", requireValidKey = true)
    public static void openTicket(final Player sender, final String[] args) {
        EnjinMinecraftPlugin plugin = EnjinMinecraftPlugin.getInstance();

        if (args.length == 0) {
            final Player player = sender;
            Bukkit.getScheduler().runTaskAsynchronously(plugin, new Runnable() {
                @Override
                public void run() {
                    TicketService service = EnjinServices.getService(TicketService.class);
                    RPCData<TicketResults> data = service.getTickets(-1, TicketStatus.open);

                    if (data != null) {
                        if (data.getError() != null) {
                            player.sendMessage(data.getError().getMessage());
                        } else {
                            List<Ticket> tickets = data.getResult().getResults();
                            if (tickets.size() > 0) {
                                TicketViewBuilder.buildTicketList(tickets).send(player);
                            } else {
                                player.sendMessage("There are no open tickets at this time.");
                            }
                        }
                    } else {
                        player.sendMessage("Could not fetch open tickets.");
                    }
                }
            });
        } else {
            final Player player = sender;
            Bukkit.getScheduler().runTaskAsynchronously(plugin, new Runnable() {
                @Override
                public void run() {
                    TicketService service = EnjinServices.getService(TicketService.class);
                    RPCData<ReplyResults> data = service.getReplies(-1, args[0], player.getName());

                    if (data != null) {
                        if (data.getError() != null) {
                            player.sendMessage(data.getError().getMessage());
                        } else {
                            List<Reply> replies = data.getResult().getResults();
                            if (replies.size() > 0) {
                                TicketViewBuilder.buildTicket(args[0], replies, player.hasPermission("enjin.ticket.private")).send(player);
                            } else {
                                player.sendMessage("You entered an invalid ticket code!");
                            }
                        }
                    } else {
                        player.sendMessage("Could not fetch ticket replies.");
                    }
                }
            });
        }
    }

    @Permission(value = "enjin.ticket.reply")
    @Directive(parent = "enjin", value = "reply", requireValidKey = true)
    public static void reply(final Player sender, final String[] args) {
        EnjinMinecraftPlugin plugin = EnjinMinecraftPlugin.getInstance();

        if (args.length < 3) {
            sender.sendMessage("Usage: /e reply <module_id> <ticket_code> <message>");
            return;
        } else {
            final int preset;

            try {
                preset = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                sender.sendMessage("Usage: /e reply <module_id> <ticket_code> <message>");
                return;
            }

            final String ticket = args[1];
            String message = "";
            for (String arg : Arrays.copyOfRange(args, 2, args.length)) {
                message = message.concat(arg + " ");
            }
            message.trim();
            final String finalMessage = message;

            Bukkit.getScheduler().runTaskAsynchronously(plugin, new Runnable() {
                @Override
                public void run() {
                    RPCData<RPCSuccess> result = EnjinServices.getService(TicketService.class).sendReply(preset, ticket, finalMessage, "public", TicketStatus.open, sender.getName());
                    if (result != null) {
                        if (result.getError() == null) {
                            sender.sendMessage("You replied to the ticket successfully.");
                        } else {
                            sender.sendMessage(result.getError().getMessage());
                        }
                    } else {
                        sender.sendMessage("Unable to submit your reply.");
                    }
                }
            });
        }
    }

    @Permission(value = "enjin.ticket.status")
    @Directive(parent = "enjin", value = "ticketstatus", requireValidKey = true)
    public static void ticketStatus(final Player sender, final String[] args) {
        EnjinMinecraftPlugin plugin = EnjinMinecraftPlugin.getInstance();

        if (args.length != 3) {
            sender.sendMessage("Usage: /e ticketstatus <preset_id> <ticket_code> <open,pending,closed>");
        } else {
            final int preset;
            try {
                preset = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                sender.sendMessage("Usage: /e ticketstatus <preset_id> <ticket_code> <open,pending,closed>");
                return;
            }

            TicketStatus tempStatus = null;
            for (TicketStatus s : TicketStatus.values()) {
                if (s.name().equalsIgnoreCase(args[2])) {
                    tempStatus = s;
                    break;
                }
            }

            if (tempStatus == null) {
                sender.sendMessage("Usage: /e ticketstatus <preset_id> <ticket_code> <open,pending,closed>");
                return;
            }

            final String ticket = args[1];
            final TicketStatus status = tempStatus;
            Bukkit.getScheduler().runTaskAsynchronously(plugin, new Runnable() {
                @Override
                public void run() {
                    RPCData<Boolean> result = EnjinServices.getService(TicketService.class).setStatus(preset, ticket, status);
                    if (result != null) {
                        if (result.getError() == null) {
                            if (result.getResult()) {
                                sender.sendMessage("The tickets status was successfully changed to " + status.name());
                            } else {
                                sender.sendMessage("The tickets status was unable to be changed to " + status.name());
                            }
                        } else {
                            sender.sendMessage(result.getError().getMessage());
                        }
                    } else {
                        sender.sendMessage("The tickets status was unable to be changed to " + status.name());
                    }
                }
            });
        }
    }
}
