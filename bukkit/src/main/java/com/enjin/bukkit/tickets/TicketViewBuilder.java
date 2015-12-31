package com.enjin.bukkit.tickets;

import com.enjin.rpc.mappings.mappings.tickets.Reply;
import com.enjin.rpc.mappings.mappings.tickets.Ticket;
import mkremins.fanciful.FancyMessage;
import org.bukkit.ChatColor;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class TicketViewBuilder {
    private static final DateFormat dateFormat = new SimpleDateFormat("hh:mm:ss dd-MM-yyyy");

    public static FancyMessage buildTicketList(List<Ticket> tickets) {
        Collections.sort(tickets, new Comparator<Ticket>() {
            @Override
            public int compare(Ticket o1, Ticket o2) {
                return Long.compare(o1.getUpdated(), o2.getUpdated());
            }
        });

        FancyMessage message = new FancyMessage("Your Tickets:\n")
                .color(ChatColor.GOLD);

        for (Ticket ticket : tickets) {
            message.then(ticket.getCode() + ") " + ticket.getSubject() + " (" + ticket.getReplyCount() + " Replies, " + getLastUpdateDisplay((System.currentTimeMillis() / 1000) - ticket.getUpdated()) + ")\n")
                    .color(ChatColor.GREEN)
                    .command("/e ticket " + ticket.getCode());
        }

        message.then("[Please click a ticket or type /e ticket <#> to view it]")
                .color(ChatColor.GOLD);

        return message;
    }

    public static FancyMessage buildTicket(String ticketCode, List<Reply> replies, boolean showPrivate) {
        Collections.sort(replies, new Comparator<Reply>() {
            @Override
            public int compare(Reply o1, Reply o2) {
                return Long.compare(o1.getSent(), o2.getSent());
            }
        });

        FancyMessage message = null;
        for (Reply reply : replies) {
            if (message == null) {
                message = new FancyMessage("---------------\n")
                        .color(ChatColor.GOLD);
            } else {
                message.then("---------------\n")
                        .color(ChatColor.GOLD);
            }

            if (!showPrivate && reply.getMode().equalsIgnoreCase("private")) {
                continue;
            }

            message.then(reply.getUsername() + ChatColor.GRAY.toString() + " (" + ChatColor.GREEN.toString() + dateFormat.format(new Date(reply.getSent() * 1000)) + ChatColor.GRAY.toString() + ")" + ChatColor.DARK_GRAY.toString() + ":\n")
                    .color(ChatColor.GREEN);
            if (showPrivate && reply.getMode().equalsIgnoreCase("private")) {
                message.then(ChatColor.DARK_GRAY.toString() + "(" + ChatColor.GRAY.toString() + "Private" + ChatColor.DARK_GRAY.toString() + ") ");
            }
            message.then(reply.getText().replaceAll("\\s+", " ").replace("<br>", "\n").replace("<b>", ChatColor.GRAY.toString() + ChatColor.BOLD.toString()).replace("</b>", ChatColor.DARK_GRAY.toString() + ":" + ChatColor.GOLD.toString()) + "\n")
                    .color(ChatColor.GOLD);
        }

        Reply reply = replies.get(0);
        message.then(ChatColor.GRAY + "[" + ChatColor.GOLD + "To reply to this ticket please type:\n");
        message.then(ChatColor.GREEN + "/e reply " + reply.getPresetId() + " " + ticketCode + " <message>")
                .command("/e reply " + reply.getPresetId() + " " + ticketCode + " <message>");
        message.then(ChatColor.GOLD + ",\nor to set the status of this ticket type:\n");
        message.then(ChatColor.GREEN + "/e ticketstatus " + reply.getPresetId() + " " + ticketCode + " <open/pending/closed>")
                .command("/e ticketstatus " + reply.getPresetId() + " " + ticketCode + " <open/pending/closed>");
        message.then(ChatColor.GRAY + "]");

        return message;
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
