package com.enjin.bukkit.command.commands;

import com.enjin.bukkit.EnjinMinecraftPlugin;
import com.enjin.bukkit.command.Directive;
import com.enjin.bukkit.command.Permission;
import com.enjin.core.EnjinServices;
import com.enjin.rpc.mappings.mappings.general.RPCData;
import com.enjin.rpc.mappings.services.PointService;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class PointCommands {
    private static final String ADD_POINTS_USAGE = "USAGE: /enjin addpoints <points> or /enjin addpoints <player> <points>";
    private static final String REMOVE_POINTS_USAGE = "USAGE: /enjin removepoints <points> or /enjin removepoints <player> <points>";
    private static final String SET_POINTS_USAGE = "USAGE: /enjin setpoints <points> or /enjin setpoints <player> <points>";

    @Permission(value = "enjin.points")
    @Directive(parent = "enjin", value = "points")
    public static void points(CommandSender sender, String[] args) {
        if (args.length == 0 && !(sender instanceof Player)) {
            sender.sendMessage("Only a player can check their own points.");
            return;
        }

        String name = args.length == 0 ? sender.getName() : args[0].substring(0, args[0].length() > 16 ? 16 : args[0].length());
        PointService service = EnjinServices.getService(PointService.class);
        RPCData<Integer> data = service.get(EnjinMinecraftPlugin.getConfiguration().getAuthKey(), name);

        if (data == null) {
            sender.sendMessage("A fatal error has occurred. Please try again later. If the problem persists please contact Enjin support.");
            return;
        }

        if (data.getError() != null) {
            sender.sendMessage(data.getError().getMessage());
            return;
        }

        sender.sendMessage(ChatColor.GOLD + (args.length == 0 ? "You have " : name + " has ") + data.getResult() + " points.");
    }

    @Permission(value = "enjin.points.add")
    @Directive(parent = "enjin", value = "addpoints")
    public static void add(CommandSender sender, String[] args) {
        String name = sender.getName();
        Integer points;
        if (args.length == 1) {
            if (!(sender instanceof Player)) {
                sender.sendMessage("Only a player can give themselves points.");
                return;
            }

            try {
                points = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                sender.sendMessage(ADD_POINTS_USAGE);
                return;
            }
        } else if (args.length >= 2) {
            name = args[0];

            try {
                points = Integer.parseInt(args[1]);
            } catch (NumberFormatException e) {
                sender.sendMessage(ADD_POINTS_USAGE);
                return;
            }
        } else {
            sender.sendMessage(ADD_POINTS_USAGE);
            return;
        }

        PointService service = EnjinServices.getService(PointService.class);
        RPCData<Integer> data = service.add(EnjinMinecraftPlugin.getConfiguration().getAuthKey(), name, points);

        if (data == null) {
            sender.sendMessage("A fatal error has occurred. Please try again later. If the problem persists please contact Enjin support.");
            return;
        }

        if (data.getError() != null) {
            sender.sendMessage(data.getError().getMessage());
            return;
        }

        sender.sendMessage(ChatColor.GREEN + (args.length == 1 ? "Your" : (name + "'s")) + " new point balance is " + ChatColor.GOLD + data.getResult().intValue());
    }

    @Permission(value = "enjin.points.remove")
    @Directive(parent = "enjin", value = "removepoints")
    public static void remove(CommandSender sender, String[] args) {
        String name = sender.getName();
        Integer points;
        if (args.length == 1) {
            if (!(sender instanceof Player)) {
                sender.sendMessage("Only a player can remove points from themselves.");
                return;
            }

            try {
                points = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                sender.sendMessage(REMOVE_POINTS_USAGE);
                return;
            }
        } else if (args.length >= 2) {
            name = args[0];

            try {
                points = Integer.parseInt(args[1]);
            } catch (NumberFormatException e) {
                sender.sendMessage(REMOVE_POINTS_USAGE);
                return;
            }
        } else {
            sender.sendMessage(REMOVE_POINTS_USAGE);
            return;
        }

        PointService service = EnjinServices.getService(PointService.class);
        RPCData<Integer> data = service.remove(EnjinMinecraftPlugin.getConfiguration().getAuthKey(), name, points);

        if (data == null) {
            sender.sendMessage("A fatal error has occurred. Please try again later. If the problem persists please contact Enjin support.");
            return;
        }

        if (data.getError() != null) {
            sender.sendMessage(data.getError().getMessage());
            return;
        }

        sender.sendMessage(ChatColor.GREEN + (args.length == 1 ? "Your" : (name + "'s")) + " new point balance is " + ChatColor.GOLD + data.getResult().intValue());
    }

    @Permission(value = "enjin.points.getself")
    @Directive(parent = "enjin", value = "getpoints")
    public static void get(CommandSender sender, String[] args) {
        String name = sender.getName();
        if (args.length == 0) {
            if (!(sender instanceof Player)) {
                sender.sendMessage("Only a player can get their own points.");
                return;
            }
        } else {
            if (!sender.isOp() && !sender.hasPermission("enjin.points.getothers")) {
                sender.sendMessage(ChatColor.RED + "You need to have the \"" + ChatColor.GOLD + "enjin.points.getothers" + ChatColor.RED + "\" or OP to run that directive.");
                return;
            }

            name = args[0];
        }

        PointService service = EnjinServices.getService(PointService.class);
        RPCData<Integer> data = service.get(EnjinMinecraftPlugin.getConfiguration().getAuthKey(), name);

        if (data == null) {
            sender.sendMessage("A fatal error has occurred. Please try again later. If the problem persists please contact Enjin support.");
            return;
        }

        if (data.getError() != null) {
            sender.sendMessage(data.getError().getMessage());
            return;
        }

        sender.sendMessage(ChatColor.GREEN + (args.length == 1 ? "Your" : (name + "'s")) + " point balance is " + ChatColor.GOLD + data.getResult().intValue());
    }

    @Permission(value = "enjin.points.set")
    @Directive(parent = "enjin", value = "setpoints")
    public static void set(CommandSender sender, String[] args) {
        String name = sender.getName();
        Integer points;
        if (args.length == 1) {
            if (!(sender instanceof Player)) {
                sender.sendMessage("Only a player can set their own points.");
                return;
            }

            try {
                points = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                sender.sendMessage(SET_POINTS_USAGE);
                return;
            }
        } else if (args.length >= 2) {
            name = args[0];

            try {
                points = Integer.parseInt(args[1]);
            } catch (NumberFormatException e) {
                sender.sendMessage(SET_POINTS_USAGE);
                return;
            }
        } else {
            sender.sendMessage(SET_POINTS_USAGE);
            return;
        }

        PointService service = EnjinServices.getService(PointService.class);
        RPCData<Integer> data = service.set(EnjinMinecraftPlugin.getConfiguration().getAuthKey(), name, points);

        if (data == null) {
            sender.sendMessage("A fatal error has occurred. Please try again later. If the problem persists please contact Enjin support.");
            return;
        }

        if (data.getError() != null) {
            sender.sendMessage(data.getError().getMessage());
            return;
        }

        sender.sendMessage(ChatColor.GREEN + (args.length == 0 ? "Your" : (name + "'s")) + " new point balance is " + ChatColor.GOLD + data.getResult().intValue());
    }
}
