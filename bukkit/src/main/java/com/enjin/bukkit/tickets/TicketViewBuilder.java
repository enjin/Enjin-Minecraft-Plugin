package com.enjin.bukkit.tickets;

import com.enjin.rpc.mappings.mappings.tickets.Reply;
import com.enjin.rpc.mappings.mappings.tickets.Ticket;
import mkremins.fanciful.FancyMessage;
import org.bukkit.ChatColor;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class TicketViewBuilder {
    private static final DateFormat dateFormat = new SimpleDateFormat("hh:mm:ss dd-MM-yyyy");

    public static List<FancyMessage> buildTicketList(List<Ticket> tickets) {
        Collections.sort(tickets, new Comparator<Ticket>() {
            @Override
            public int compare(Ticket o1, Ticket o2) {
                return Long.compare(o1.getUpdated(), o2.getUpdated());
            }
        });

        List<FancyMessage> messages = new ArrayList<>();

        FancyMessage message = new FancyMessage("Your Tickets:")
                .color(ChatColor.GOLD);
        messages.add(message);

        for (Ticket ticket : tickets) {
            message = new FancyMessage(ticket.getCode() + ") " + ticket.getSubject() + " (" + ticket.getReplyCount() + " Replies, " + getLastUpdateDisplay((System.currentTimeMillis() / 1000) - ticket.getUpdated()) + ")")
                    .color(ChatColor.GREEN)
                    .command("/e ticket " + ticket.getCode());
            messages.add(message);
        }

        message = new FancyMessage("[Please click a ticket or type /e ticket <#> to view it]")
                .color(ChatColor.GOLD);
        messages.add(message);

        return messages;
    }

    public static List<FancyMessage> buildTicket(String ticketCode, List<Reply> replies, boolean showPrivate) {
        Collections.sort(replies, new Comparator<Reply>() {
            @Override
            public int compare(Reply o1, Reply o2) {
                return Long.compare(o1.getSent(), o2.getSent());
            }
        });

        List<FancyMessage> messages = new ArrayList<>();

        FancyMessage message = null;
        for (Reply reply : replies) {
            message = new FancyMessage("---------------")
                    .color(ChatColor.GOLD);
            messages.add(message);

            if (!showPrivate && reply.getMode().equalsIgnoreCase("private")) {
                continue;
            }

            message = new FancyMessage(reply.getUsername())
                    .color(ChatColor.GOLD)
                    .then(" (")
                    .color(ChatColor.GRAY)
                    .then(dateFormat.format(new Date(reply.getSent() * 1000)))
                    .color(ChatColor.GREEN)
                    .then(")")
                    .color(ChatColor.GRAY)
                    .then(":")
                    .color(ChatColor.DARK_GRAY);
            messages.add(message);

            if (showPrivate && reply.getMode().equalsIgnoreCase("private")) {
                message = new FancyMessage("(")
                        .color(ChatColor.DARK_GRAY)
                        .then("Private")
                        .color(ChatColor.GRAY)
                        .then(")")
                        .color(ChatColor.DARK_GRAY);
                message.text(ChatColor.DARK_GRAY.toString() + "(" + ChatColor.GRAY.toString() + "Private" + ChatColor.DARK_GRAY.toString() + ") ");
            } else {
                message = null;
            }

            String text = reply.getText().replaceAll("\\s+", " ");
            String[] parts = text.split("<br>");
            for (String part : parts) {
                String line = part.replace("<b>", ChatColor.GRAY.toString() + ChatColor.BOLD.toString()).replace("</b>", ChatColor.GRAY.toString()) + '\n';
                if (showPrivate && message != null) {
                    message.then(line);
                } else {
                    message = new FancyMessage(line);
                }

                message.color(ChatColor.GRAY);
            }

            if (message != null) {
                messages.add(message);
            }
        }

        Reply reply = replies.get(0);
        message = new FancyMessage("[")
                .color(ChatColor.GRAY)
                .then("To reply to this ticket please type:")
                .color(ChatColor.GOLD);
        messages.add(message);
        message = new FancyMessage("/e reply " + reply.getPresetId() + " " + ticketCode + " <message>")
                .color(ChatColor.GREEN)
                .suggest("/e reply " + reply.getPresetId() + " " + ticketCode + " <message>");
        messages.add(message);
        message = new FancyMessage("or to set the status of this ticket type:")
                .color(ChatColor.GOLD);
        messages.add(message);
        message = new FancyMessage("/e ticketstatus " + reply.getPresetId() + " " + ticketCode + " <open/pending/closed>")
                .color(ChatColor.GREEN)
                .suggest("/e ticketstatus " + reply.getPresetId() + " " + ticketCode + " <open/pending/closed>")
                .then("]")
                .color(ChatColor.GRAY);
        messages.add(message);

        return messages;
    }

    private static String getLastUpdateDisplay(long time) {
        if (time < 60) {
            return "Just Now";
        } else if (time < 60 * 60) {
            return TimeUnit.SECONDS.toMinutes(time) + " minutes ago";
        } else if (time < 24 * 60 * 60) {
            return TimeUnit.SECONDS.toHours(time) + " hours ago";
        } else if (time < 365 * 24 * 60 * 60) {
            return TimeUnit.SECONDS.toDays(time) + " days ago";
        } else {
            return (TimeUnit.SECONDS.toDays(time) / 365) + " years ago";
        }
    }
}
