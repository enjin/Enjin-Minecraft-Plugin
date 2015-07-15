package com.enjin.officialplugin.tickets;

import com.enjin.core.EnjinServices;
import com.enjin.officialplugin.EnjinMinecraftPlugin;
import com.enjin.rpc.mappings.mappings.general.RPCResult;
import com.enjin.rpc.mappings.mappings.general.ResultType;
import com.enjin.rpc.mappings.mappings.tickets.ExtraQuestion;
import com.enjin.rpc.mappings.mappings.tickets.QuestionType;
import com.enjin.rpc.mappings.services.TicketsService;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.*;

public class TicketSubmission {
    public static void submit(Player player, int moduleId, List<QuestionResponse> responses) {
        List<QuestionResponse> answers = new ArrayList<QuestionResponse>(responses);
        TicketsService service = EnjinServices.getService(TicketsService.class);

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
                    continue;
                } else if (description == null && response.getQuestion().getType() == QuestionType.multiline) {
                    description = response;
                    answers.remove(response);
                    continue;
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

        player.sendMessage("\n" + ChatColor.GOLD + "Your ticket is being submitted!");
        RPCResult result = service.createTicket(EnjinMinecraftPlugin.getHash(), moduleId, (String) subject.getAnswer(), (String) description.getAnswer(), player.getName(), extra);
        if (player != null) {
            player.sendMessage(result.getMessage());

            if (!(result.getType() == ResultType.SUCCESS)) {
                EnjinMinecraftPlugin.debug("Request: " + (result.getRequest() == null ? "null" : result.getRequest().toJSONString()));
                EnjinMinecraftPlugin.debug("Response: " + (result.getResponse() == null ? "null" : result.getResponse().toJSONString()));
            }
        }
    }
}
