package com.enjin.bukkit.tickets;

import com.enjin.rpc.mappings.mappings.tickets.Reply;
import com.enjin.rpc.mappings.mappings.tickets.Ticket;
import net.kyori.text.TextComponent;
import net.kyori.text.event.ClickEvent;
import net.kyori.text.event.HoverEvent;
import net.kyori.text.format.TextColor;
import net.kyori.text.format.TextDecoration;
import net.kyori.text.serializer.ComponentSerializers;
import net.kyori.text.serializer.LegacyComponentSerializer;
import org.bukkit.ChatColor;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class TicketViewBuilder {
    private static final DateFormat dateFormat = new SimpleDateFormat("hh:mm:ss dd-MM-yyyy");
    private static final LegacyComponentSerializer LEGACY_COMPONENT_SERIALIZER = ComponentSerializers.LEGACY;

    public static List<TextComponent> buildTicketList(List<Ticket> tickets) {
        Collections.sort(tickets, new Comparator<Ticket>() {
            @Override
            public int compare(Ticket o1, Ticket o2) {
                return Long.compare(o1.getUpdated(), o2.getUpdated());
            }
        });

        List<TextComponent> messages = new ArrayList<>();

        TextComponent message = TextComponent.of("Your Tickets:")
                .color(TextColor.GOLD);
        messages.add(message);

        for (Ticket ticket : tickets) {
            message = TextComponent.of(ticket.getCode() + ") " + ticket.getSubject() + " (" + ticket.getReplyCount() + " Replies, " + getLastUpdateDisplay((System.currentTimeMillis() / 1000) - ticket.getUpdated()) + ")")
                    .color(TextColor.GREEN)
                    .clickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/e ticket " + ticket.getCode()));
            messages.add(message);
        }

        message = TextComponent.of("[Please click a ticket or type /e ticket <#> to view it]")
                .color(TextColor.GOLD);
        messages.add(message);

        return messages;
    }

    public static List<TextComponent> buildTicket(String ticketCode, List<Reply> replies, boolean showPrivate) {
        Collections.sort(replies, new Comparator<Reply>() {
            @Override
            public int compare(Reply o1, Reply o2) {
                return Long.compare(o1.getSent(), o2.getSent());
            }
        });

        List<TextComponent> messages = new ArrayList<>();

        TextComponent message = null;
        for (Reply reply : replies) {
            message = TextComponent.of("---------------")
                    .color(TextColor.GOLD);
            messages.add(message);

            if (!showPrivate && reply.getMode().equalsIgnoreCase("private")) {
                continue;
            }

            message = TextComponent.of(reply.getUsername())
                    .color(TextColor.GOLD)
                    .append(TextComponent.of(" (").color(TextColor.GRAY))
                    .append(TextComponent.of(dateFormat.format(new Date(reply.getSent() * 1000))).color(TextColor.GREEN))
                    .append(TextComponent.of(")").color(TextColor.GRAY))
                    .append(TextComponent.of(":").color(TextColor.DARK_GRAY));
            messages.add(message);

            if (showPrivate && reply.getMode().equalsIgnoreCase("private")) {
                message = TextComponent.of("(")
                        .color(TextColor.DARK_GRAY)
                        .append(TextComponent.of("Private").color(TextColor.GRAY))
                        .append(TextComponent.of(")").color(TextColor.DARK_GRAY));
            } else {
                message = null;
            }

            String text = reply.getText().replaceAll("\\s+", " ");
            String[] parts = text.split("<br>");
            for (String part : parts) {
                TextComponent comp = LEGACY_COMPONENT_SERIALIZER.deserialize(part.replace("<b>", ChatColor.GRAY.toString() + ChatColor.BOLD.toString()).replace("</b>", ChatColor.GRAY.toString()) + '\n');
                if (showPrivate && message != null) {
                    message.append(comp);
                } else {
                    message = comp;
                }

                message.color(TextColor.GRAY);
            }

            if (message != null) {
                messages.add(message);
            }
        }

        Reply reply = replies.get(0);
        message = TextComponent.of("[")
                .color(TextColor.GRAY)
                .append(TextComponent.of("to reply to this ticket please type:").color(TextColor.GOLD));
        messages.add(message);
        message = TextComponent.of("/e reply " + reply.getPresetId() + " " + ticketCode + " <message>")
                .color(TextColor.GREEN)
                .clickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/e reply " + reply.getPresetId() + " " + ticketCode + " <message>"));
        messages.add(message);
        message = TextComponent.of("or to set the status of this ticket type:")
                .color(TextColor.GOLD);
        messages.add(message);
        message = TextComponent.of("/e ticketstatus " + reply.getPresetId() + " " + ticketCode + " <open/pending/closed>")
                .color(TextColor.GREEN)
                .clickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/e ticketstatus " + reply.getPresetId() + " " + ticketCode + " <open/pending/closed>"))
                .append(TextComponent.of("]").color(TextColor.GRAY));
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
