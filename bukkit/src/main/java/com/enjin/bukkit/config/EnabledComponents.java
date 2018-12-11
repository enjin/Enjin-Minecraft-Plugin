package com.enjin.bukkit.config;

import com.google.gson.annotations.SerializedName;
import lombok.Getter;

public class EnabledComponents {
    @Getter
    @SerializedName("vote-listener")
    private boolean voteListener = true;
}
