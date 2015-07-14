package com.enjin.rpc.mappings.mappings.tickets;

import com.google.gson.annotations.SerializedName;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.util.List;

@ToString
@EqualsAndHashCode
public class Question {
    @Getter
    private int id;
    @Getter
    @SerializedName(value = "site_id")
    private int siteId;
    @Getter
    @SerializedName(value = "preset_id")
    private int presetId;
    @Getter
    private QuestionType type;
    @Getter
    private String label;
    @Getter
    @SerializedName(value = "help_text")
    private String helpText;
    @Getter
    private boolean required;
    @Getter
    private int order;
    @Getter
    @SerializedName(value = "other_options")
    private MetaOptions otherOptions;
    @Getter
    private List<String> options;
    @Getter
    private List<Condition> conditions;
    @Getter
    @SerializedName(value = "condition_qualify")
    private ConditionQualify conditionQualify;
    @Getter
    private int system;
}
