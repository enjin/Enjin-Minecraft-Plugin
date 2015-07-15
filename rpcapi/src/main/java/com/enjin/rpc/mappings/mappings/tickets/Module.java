package com.enjin.rpc.mappings.mappings.tickets;

import com.google.gson.annotations.SerializedName;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ToString
@EqualsAndHashCode
public class Module {
    @Getter
    private List<Question> questions;
    @Getter
    @SerializedName(value = "module_name")
    private String name;
    @Getter
    private String command;
    @Getter
    @SerializedName(value = "command_help")
    private String help;

    public Map<Integer, Question> getIdMappedQuestions() {
        Map<Integer, Question> map = new HashMap<Integer, Question>();

        if (questions != null) {
            for (Question question : questions) {
                map.put(question.getId(), question);
            }
        }

        return map;
    }
}
