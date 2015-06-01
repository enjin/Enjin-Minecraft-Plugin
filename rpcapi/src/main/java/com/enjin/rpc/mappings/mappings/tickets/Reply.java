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
    private int agent;
    @Getter
    @SerializedName(value = "preset_id")
    private int presetId;
    @Getter
    @SerializedName(value = "user_id")
    private int userId;
    @Getter
    private int origin;
    @Getter
    private int id;
    @Getter
    private String text;
    @Getter
    private long sent;
    @Getter
    private String username;
}
