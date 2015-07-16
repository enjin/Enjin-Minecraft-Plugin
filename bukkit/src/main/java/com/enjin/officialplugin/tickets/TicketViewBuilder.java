package com.enjin.officialplugin.tickets;

import com.enjin.rpc.mappings.mappings.tickets.Reply;
import com.enjin.rpc.mappings.mappings.tickets.Ticket;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class TicketViewBuilder {
    private static final DateFormat dateFormat = new SimpleDateFormat("hh:mm:ss dd-MM-yyyy");

    public static BaseComponent[] buildTicketList(List<Ticket> tickets) {
        Collections.sort(tickets, new Comparator<Ticket>() {
            @Override
            public int compare(Ticket o1, Ticket o2) {
                return Long.compare(o1.getUpdated(), o2.getUpdated());
            }
        });

        ComponentBuilder builder = new ComponentBuilder("Your Tickets:\n")
                .color(ChatColor.GOLD);

        for (Ticket ticket : tickets) {
            builder.append(ticket.getCode() + ") " + ticket.getSubject() + " (" + ticket.getReplyCount() + " Replies, " + getLastUpdateDisplay((System.currentTimeMillis() / 1000) - ticket.getUpdated()) + " ago)\n")
                    .color(ChatColor.GREEN)
                    .event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/e ticket " + ticket.getCode()));
        }

        builder.append("[Please click a ticket or type /e ticket <#> to view it]")
                .color(ChatColor.GOLD);

        return builder.create();
    }

    public static BaseComponent[] buildTicket(List<Reply> replies, boolean showPrivate) {
        Collections.sort(replies, new Comparator<Reply>() {
            @Override
            public int compare(Reply o1, Reply o2) {
                return Long.compare(o1.getSent(), o2.getSent());
            }
        });

        ComponentBuilder builder = null;

        for (Reply reply : replies) {
            if (builder == null) {
                builder = new ComponentBuilder("---------------\n")
                        .color(ChatColor.GOLD);
            } else {
                builder.append("---------------\n")
                        .color(ChatColor.GOLD);
            }

            builder.append(reply.getUsername() + ChatColor.GRAY.toString() + " (" + ChatColor.GREEN.toString() + dateFormat.format(new Date(reply.getSent() * 1000)) + ChatColor.GRAY.toString() + ")" + ChatColor.DARK_GRAY.toString() + ":\n")
                    .color(ChatColor.GREEN);
            if (showPrivate && reply.getMode().equalsIgnoreCase("private")) {
                builder.append(ChatColor.DARK_GRAY.toString() + "(" + ChatColor.GRAY.toString() + "Private" + ChatColor.DARK_GRAY.toString() + ") ");
            }
            builder.append(reply.getText().replaceAll("\\s+", " ").replace("<br>", "\n").replace("<b>", ChatColor.GRAY.toString() + ChatColor.BOLD.toString()).replace("</b>", ChatColor.DARK_GRAY.toString() + ":" + ChatColor.GOLD.toString()) + "\n")
                    .color(ChatColor.GOLD);
        }

        return builder.create();
    }

    private static String getLastUpdateDisplay(long time) {
        if (time < 60) {
            return time + " seconds";
        } else if (time < 60 * 60) {
            return TimeUnit.SECONDS.toMinutes(time) + " minutes";
        } else if (time < 24 * 60 * 60) {
            return TimeUnit.SECONDS.toHours(time) + " hours";
        } else if (time < 365 * 24 * 60 * 60) {
            return TimeUnit.SECONDS.toDays(time) + " days";
        } else {
            return (TimeUnit.SECONDS.toDays(time) / 365) + " years";
        }
    }
}
