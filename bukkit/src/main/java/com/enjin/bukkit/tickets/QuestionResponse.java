package com.enjin.bukkit.tickets;

import com.enjin.rpc.mappings.mappings.tickets.Question;
import lombok.Getter;

public class QuestionResponse {
    @Getter
    private Question question;
    @Getter
    private Object   answer;

    public QuestionResponse(Question question, Object answer) {
        this.question = question;
        this.answer = answer;
    }
}
