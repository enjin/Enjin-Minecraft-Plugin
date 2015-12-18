package com.enjin.rpc.mappings.mappings.tickets;

import com.google.gson.annotations.SerializedName;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@ToString
@EqualsAndHashCode
public class Reply {
    @Getter
    private String mode;
    @Getter
    private Integer agent;
    @Getter
    @SerializedName(value = "preset_id")
    private Integer presetId;
    @Getter
    @SerializedName(value = "user_id")
    private Integer userId;
    @Getter
    private Integer origin;
    @Getter
    private Integer id;
    @Getter
    private String text;
    @Getter
    private Long sent;
    @Getter
    private String username;
}
