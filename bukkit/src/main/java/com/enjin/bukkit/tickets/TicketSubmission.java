package com.enjin.bukkit.tickets;

import com.enjin.core.EnjinServices;
import com.enjin.rpc.mappings.mappings.general.RPCData;
import com.enjin.rpc.mappings.mappings.general.RPCSuccess;
import com.enjin.rpc.mappings.mappings.tickets.ExtraQuestion;
import com.enjin.rpc.mappings.mappings.tickets.QuestionType;
import com.enjin.rpc.mappings.services.TicketService;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.*;

public class TicketSubmission {
    public static void submit(Player player, int moduleId, List<QuestionResponse> responses) {
        List<QuestionResponse> answers = new ArrayList<QuestionResponse>(responses);
        TicketService service = EnjinServices.getService(TicketService.class);

        Collections.sort(responses, new Comparator<QuestionResponse>() {
            @Override
            public int compare(QuestionResponse o1, QuestionResponse o2) {
                return Integer.compare(o1.getQuestion().getId(), o2.getQuestion().getId());
            }
        });

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

        List<ExtraQuestion> extra = new ArrayList<ExtraQuestion>();
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

        player.sendMessage("\n" + ChatColor.GOLD + "Submitting ticket...");
        RPCData<RPCSuccess> result = service.createTicket(moduleId, (String) subject.getAnswer(), (String) description.getAnswer(), player.getName(), extra);
        if (player != null) {
            if (result != null) {
                if (result.getError() != null) {
                    player.sendMessage(ChatColor.RED + result.getError().getMessage());
                } else {
                    player.sendMessage(ChatColor.GREEN + "Your ticket was submitted successfully!");
                }
            } else {
                player.sendMessage(ChatColor.RED + "Unable to submit your ticket.");
            }
        }
    }
}
