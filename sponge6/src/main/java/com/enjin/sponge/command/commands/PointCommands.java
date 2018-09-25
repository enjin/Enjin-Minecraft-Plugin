package com.enjin.sponge.command.commands;

import com.enjin.core.Enjin;
import com.enjin.core.EnjinServices;
import com.enjin.rpc.mappings.mappings.general.RPCData;
import com.enjin.rpc.mappings.services.PointService;
import com.enjin.sponge.command.Directive;
import com.enjin.sponge.command.Permission;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

public class PointCommands {
    private static final String ADD_POINTS_USAGE    = "USAGE: /enjin addpoints <points> or /enjin addpoints <player> <points>";
    private static final String REMOVE_POINTS_USAGE = "USAGE: /enjin removepoints <points> or /enjin removepoints <player> <points>";
    private static final String SET_POINTS_USAGE    = "USAGE: /enjin setpoints <points> or /enjin setpoints <player> <points>";

    @Permission(value = "enjin.points.add")
    @Directive(parent = "enjin", value = "addpoints")
    public static void add(final CommandSource sender, final String[] args) {
        final String  name;
        final Integer points;
        if (args.length == 1) {
            if (!(sender instanceof Player)) {
                sender.sendMessage(Text.of(TextColors.RED, "Only a player can give themselves points."));
                return;
            }

            try {
                name = sender.getName();
                points = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                sender.sendMessage(Text.of(ADD_POINTS_USAGE));
                return;
            }
        } else if (args.length >= 2) {
            name = args[0];

            try {
                points = Integer.parseInt(args[1]);
            } catch (NumberFormatException e) {
                sender.sendMessage(Text.of(ADD_POINTS_USAGE));
                return;
            }
        } else {
            sender.sendMessage(Text.of(ADD_POINTS_USAGE));
            return;
        }

        if (points <= 0) {
            sender.sendMessage(Text.of(TextColors.RED, "You must specify a value greater than 0 for points."));
            return;
        }

        Sponge.getScheduler().createTaskBuilder().execute(() -> {
            PointService     service = EnjinServices.getService(PointService.class);
            RPCData<Integer> data    = service.add(name, points);

            if (data == null) {
                sender.sendMessage(Text.of(TextColors.RED,
                                           "A fatal error has occurred. Please try again later. If the problem persists please contact Enjin support."));
                return;
            }

            if (data.getError() != null) {
                sender.sendMessage(Text.of(TextColors.RED, data.getError().getMessage()));
                return;
            }

            sender.sendMessage(Text.of(TextColors.GREEN,
                                       args.length == 1 ? "Your" : (name + "'s"),
                                       " new point balance is ",
                                       TextColors.GOLD,
                                       data.getResult().intValue()));
        }).async().submit(Enjin.getPlugin());
    }

    @Permission(value = "enjin.points.remove")
    @Directive(parent = "enjin", value = "removepoints")
    public static void remove(final CommandSource sender, final String[] args) {
        final String  name;
        final Integer points;
        if (args.length == 1) {
            if (!(sender instanceof Player)) {
                sender.sendMessage(Text.of(TextColors.RED, "Only a player can remove points from themselves."));
                return;
            }

            try {
                name = sender.getName();
                points = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                sender.sendMessage(Text.of(REMOVE_POINTS_USAGE));
                return;
            }
        } else if (args.length >= 2) {
            name = args[0];

            try {
                points = Integer.parseInt(args[1]);
            } catch (NumberFormatException e) {
                sender.sendMessage(Text.of(REMOVE_POINTS_USAGE));
                return;
            }
        } else {
            sender.sendMessage(Text.of(REMOVE_POINTS_USAGE));
            return;
        }

        if (points <= 0) {
            sender.sendMessage(Text.of(TextColors.RED, "You must specify a value greater than 0 for points."));
            return;
        }

        Sponge.getScheduler().createTaskBuilder().execute(() -> {
            PointService     service = EnjinServices.getService(PointService.class);
            RPCData<Integer> data    = service.remove(name, points);

            if (data == null) {
                sender.sendMessage(Text.of(TextColors.RED,
                                           "A fatal error has occurred. Please try again later. If the problem persists please contact Enjin support."));
                return;
            }

            if (data.getError() != null) {
                sender.sendMessage(Text.of(TextColors.RED, data.getError().getMessage()));
                return;
            }

            sender.sendMessage(Text.of(TextColors.GREEN,
                                       args.length == 1 ? "Your" : (name + "'s"),
                                       " new point balance is ",
                                       TextColors.GOLD,
                                       data.getResult().intValue()));
        }).async().submit(Enjin.getPlugin());
    }

    @Permission(value = "enjin.points.getself")
    @Directive(parent = "enjin", value = "getpoints", aliases = {"points"})
    public static void get(final CommandSource sender, final String[] args) {
        final String name;
        if (args.length == 0) {
            if (!(sender instanceof Player)) {
                sender.sendMessage(Text.of(TextColors.RED, "Only a player can get their own points."));
                return;
            }

            name = sender.getName();
        } else {
            if (!sender.hasPermission("enjin.points.getothers")) {
                sender.sendMessage(Text.of(TextColors.RED,
                                           "You need to have the \"",
                                           TextColors.GOLD,
                                           "enjin.points.getothers",
                                           TextColors.RED,
                                           "\" or OP to run that directive."));
                return;
            }

            name = args[0];
        }

        Sponge.getScheduler().createTaskBuilder().execute(() -> {
            PointService     service = EnjinServices.getService(PointService.class);
            RPCData<Integer> data    = service.get(name);

            if (data == null) {
                sender.sendMessage(Text.of(TextColors.RED,
                                           "A fatal error has occurred. Please try again later. If the problem persists please contact Enjin support."));
                return;
            }

            if (data.getError() != null) {
                sender.sendMessage(Text.of(TextColors.RED, data.getError().getMessage()));
                return;
            }

            sender.sendMessage(Text.of(TextColors.GREEN,
                                       args.length == 0 ? "Your" : (name + "'s"),
                                       " point balance is ",
                                       TextColors.GOLD,
                                       data.getResult().intValue()));
        }).async().submit(Enjin.getPlugin());
    }

    @Permission(value = "enjin.points.set")
    @Directive(parent = "enjin", value = "setpoints")
    public static void set(final CommandSource sender, final String[] args) {
        final String  name;
        final Integer points;
        if (args.length == 1) {
            if (!(sender instanceof Player)) {
                sender.sendMessage(Text.of(TextColors.RED, "Only a player can set their own points."));
                return;
            }

            try {
                name = sender.getName();
                points = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                sender.sendMessage(Text.of(SET_POINTS_USAGE));
                return;
            }
        } else if (args.length >= 2) {
            name = args[0];

            try {
                points = Integer.parseInt(args[1]);
            } catch (NumberFormatException e) {
                sender.sendMessage(Text.of(SET_POINTS_USAGE));
                return;
            }
        } else {
            sender.sendMessage(Text.of(SET_POINTS_USAGE));
            return;
        }

        if (points < 0) {
            sender.sendMessage(Text.of(TextColors.RED, "You cannot set points to less than 0."));
            return;
        }

        Sponge.getScheduler().createTaskBuilder().execute(new Runnable() {
            @Override
            public void run() {
                PointService     service = EnjinServices.getService(PointService.class);
                RPCData<Integer> data    = service.set(name, points);

                if (data == null) {
                    sender.sendMessage(Text.of(TextColors.RED,
                                               "A fatal error has occurred. Please try again later. If the problem persists please contact Enjin support."));
                    return;
                }

                if (data.getError() != null) {
                    sender.sendMessage(Text.of(TextColors.RED, data.getError().getMessage()));
                    return;
                }

                sender.sendMessage(Text.of(TextColors.GREEN,
                                           args.length == 0 ? "Your" : (name + "'s"),
                                           " new point balance is ",
                                           TextColors.GOLD,
                                           data.getResult().intValue()));
            }
        }).async().submit(Enjin.getPlugin());
    }
}
