package com.enjin.sponge.tickets;

import com.enjin.rpc.mappings.mappings.tickets.Reply;
import com.enjin.rpc.mappings.mappings.tickets.Ticket;
import com.enjin.sponge.utils.text.TextUtils;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.action.TextActions;
import org.spongepowered.api.text.format.TextColors;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class TicketViewBuilder {
    private static final DateFormat dateFormat = new SimpleDateFormat("hh:mm:ss dd-MM-yyyy");

    public static Text buildTicketList(List<Ticket> tickets) {
        Collections.sort(tickets, (o1, o2) -> Long.compare(o1.getUpdated(), o2.getUpdated()));

        Text.Builder builder = Text.builder();

        Text message = Text.of(TextColors.GOLD, "Your Tickets:");
        builder.append(message).append(Text.NEW_LINE);

        for (Ticket ticket : tickets) {
            message = Text.of(TextColors.GREEN,
                              ticket.getCode(),
                              ") ",
                              ticket.getSubject(),
                              " (",
                              ticket.getReplyCount(),
                              " Replies, ",
                              getLastUpdateDisplay((System.currentTimeMillis() / 1000) - ticket.getUpdated()),
                              ")");
            message = Text.builder().append(message)
                          .onClick(TextActions.runCommand("/e ticket " + ticket.getCode()))
                          .build();
            builder.append(message).append(Text.NEW_LINE);
        }

        message = Text.of(TextColors.GOLD, "[Please click a ticket or type /e ticket <#> to view it]");
        builder.append(message);

        return builder.build();
    }

    public static Text buildTicket(String ticketCode, List<Reply> replies, boolean showPrivate) {
        Collections.sort(replies, (o1, o2) -> Long.compare(o1.getSent(), o2.getSent()));

        Text.Builder builder = Text.builder();

        for (Reply reply : replies) {
            Text message = Text.of(TextColors.GOLD, "---------------", Text.NEW_LINE);
            builder.append(message);

            if (!showPrivate && reply.getMode().equalsIgnoreCase("private")) {
                continue;
            }

            message = Text.of(TextColors.GOLD, reply.getUsername(),
                              TextColors.GRAY, " (",
                              TextColors.GREEN, dateFormat.format(new Date(reply.getSent() * 1000)),
                              TextColors.GRAY, ")",
                              TextColors.DARK_GRAY, ":",
                              Text.NEW_LINE);
            builder.append(message);

            if (showPrivate && reply.getMode().equalsIgnoreCase("private")) {
                message = Text.of(TextColors.DARK_GRAY, "(",
                                  TextColors.GRAY, "Private",
                                  TextColors.DARK_GRAY, ")",
                                  Text.NEW_LINE);
                builder.append(message);
            }

            String   text  = reply.getText().replaceAll("\\s+", " ");
            String[] parts = text.split("<br>");
            for (String part : parts) {
                String line = part.replace("<b>", "&7&l").replace("</b>", "&7");
                message = Text.of(TextColors.GRAY, TextUtils.translateText(line));
                builder.append(message).append(Text.NEW_LINE);
            }
        }

        Reply reply = replies.get(0);
        Text message = Text.of(TextColors.GRAY, "[",
                               TextColors.GOLD, "To reply to this ticket please type:");
        builder.append(message);

        message = Text.of(TextColors.GREEN, "/e reply ", reply.getPresetId(), ' ', ticketCode, " <message>");
        message = Text.builder().append(message).onClick(TextActions.suggestCommand(message.toPlain())).build();
        builder.append(Text.NEW_LINE).append(message);

        message = Text.of(TextColors.GOLD, "or to set the status of this ticket type:");
        builder.append(Text.NEW_LINE).append(message);

        message = Text.of(TextColors.GREEN,
                          "/e ticketstatus ",
                          reply.getPresetId(),
                          ' ',
                          ticketCode,
                          " <open/pending/closed>");
        message = Text.builder().append(message)
                      .onClick(TextActions.suggestCommand(message.toPlain()))
                      .append(Text.of(TextColors.GRAY, "]")).build();
        builder.append(Text.NEW_LINE).append(message);

        return builder.build();
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
