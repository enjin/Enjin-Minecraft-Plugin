package com.enjin.bungee.config;

import com.enjin.core.config.JsonConfig;
import com.google.gson.annotations.SerializedName;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@ToString
public class EnjinConfig extends JsonConfig {
    @Getter
    @Setter
    private boolean debug = false;
    @Getter @Setter
    @SerializedName(value = "auth-key")
    private String authKey = "";
    @Getter @Setter
    private boolean https = true;
    @Getter @Setter
    @SerializedName(value = "auto-update")
    private boolean autoUpdate = true;
    @Getter @Setter
    @SerializedName(value = "logging-enabled")
    private boolean loggingEnabled = true;
    @Getter @Setter
    @SerializedName(value = "api-url")
    private String apiUrl = "://api.enjin.com/api/v1";
}
