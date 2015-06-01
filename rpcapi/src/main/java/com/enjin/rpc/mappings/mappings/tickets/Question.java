package com.enjin.rpc.mappings.mappings.tickets;

import com.google.gson.annotations.SerializedName;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

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
    private boolean required;
    @Getter
    private int order;
    @Getter
    @SerializedName(value = "other_options")
    private String otherOptions;
    @Getter
    private String options;
    @Getter
    private String conditions;
    @Getter
    @SerializedName(value = "condition_qualify")
    private String conditionQualify;
    @Getter
    private int system;
}
