package com.enjin.rpc.mappings.mappings.plugin.data;

import com.google.gson.annotations.SerializedName;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class ExecuteData {
    @Getter
    @SerializedName(value = "command_id")
    private Long id = -1L;
    @Getter
    private String command = "";
    @Getter
    private Long delay = 0L;
    @Getter
    private String player;
    @Getter
    private String uuid;
    @Getter
    @SerializedName(value = "require_online")
    private Boolean requireOnline;
}
