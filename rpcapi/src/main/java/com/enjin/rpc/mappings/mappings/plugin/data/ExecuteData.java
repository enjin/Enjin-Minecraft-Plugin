package com.enjin.rpc.mappings.mappings.plugin.data;

import com.google.gson.annotations.SerializedName;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public class ExecuteData {
    @Getter
    @SerializedName(value = "command_id")
    private long id = -1;
    @Getter
    private String command = "";
    @Getter
    private long delay = 0;
}
