package com.enjin.rpc.mappings.mappings.tickets;

import com.google.gson.annotations.SerializedName;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@ToString
@EqualsAndHashCode
@NoArgsConstructor
public class ExtraQuestion {
    public ExtraQuestion(QuestionType type, Integer questionId, String label, String... answer) {
        this(type, questionId, label, Object.class.cast(answer));
    }

    public ExtraQuestion(QuestionType type, Integer questionId, String label, String answer) {
        this(type, questionId, label, Object.class.cast(answer));
    }

    private ExtraQuestion(QuestionType type, Integer questionId, String label, Object answer) {
        this.type = type;
        this.questionId = questionId;
        this.label = label;
        this.answer = answer;
    }

    @Getter
    private QuestionType type;
    @Getter
    @SerializedName("question_id")
    private Integer      questionId;
    @Getter
    private String       label;
    @Getter
    private Object       answer;
}
