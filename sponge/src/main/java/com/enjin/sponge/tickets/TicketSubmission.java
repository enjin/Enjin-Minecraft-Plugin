package com.enjin.sponge.tickets;

import com.enjin.core.EnjinServices;
import com.enjin.rpc.mappings.mappings.general.RPCData;
import com.enjin.rpc.mappings.mappings.tickets.ExtraQuestion;
import com.enjin.rpc.mappings.mappings.tickets.QuestionType;
import com.enjin.rpc.mappings.services.TicketService;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TicketSubmission {
    public static void submit(Player player, int moduleId, List<QuestionResponse> responses) {
        List<QuestionResponse> answers = new ArrayList<QuestionResponse>(responses);
        TicketService service = EnjinServices.getService(TicketService.class);

        Collections.sort(responses, (o1, o2) -> Integer.compare(o1.getQuestion().getId(), o2.getQuestion().getId()));

        QuestionResponse subject = null;
        QuestionResponse description = null;

        for (QuestionResponse response : responses) {
            if (response.getQuestion().getSystem() == 1) {
                if (subject == null && (response.getQuestion().getType() == QuestionType.select || response.getQuestion().getType() == QuestionType.text)) {
                    subject = response;
                    answers.remove(response);
                } else if (description == null && response.getQuestion().getType() == QuestionType.multiline) {
                    description = response;
                    answers.remove(response);
                }
            }
        }

        List<ExtraQuestion> extra = new ArrayList<>();
        for (QuestionResponse response : answers) {
            if (response.getQuestion().getSystem() == 0) {
                if (response.getAnswer() instanceof String) {
                    String answer = (String) response.getAnswer();
                    extra.add(new ExtraQuestion(response.getQuestion().getType(), response.getQuestion().getId(), response.getQuestion().getLabel(), answer));
                } else if (response.getAnswer() instanceof String[]) {
                    String[] answer = (String[]) response.getAnswer();
                    extra.add(new ExtraQuestion(response.getQuestion().getType(), response.getQuestion().getId(), response.getQuestion().getLabel(), answer));
                } else {
                    extra.add(new ExtraQuestion(response.getQuestion().getType(), response.getQuestion().getId(), response.getQuestion().getLabel(), response.getAnswer().toString()));
                }
            }
        }

        player.sendMessage(Text.of(Text.NEW_LINE, TextColors.GOLD, "Submitting ticket..."));
        RPCData<Boolean> result = service.createTicket(moduleId, (String) subject.getAnswer(), (String) description.getAnswer(), player.getName(), extra);
        if (player != null) {
            if (result != null) {
                if (result.getError() != null) {
                    player.sendMessage(Text.of(TextColors.RED, result.getError().getMessage()));
                } else {
                    player.sendMessage(Text.of(TextColors.GREEN, "Your ticket was submitted successfully!"));
                }
            } else {
                player.sendMessage(Text.of(TextColors.RED, "Unable to submit your ticket."));
            }
        }
    }
}
