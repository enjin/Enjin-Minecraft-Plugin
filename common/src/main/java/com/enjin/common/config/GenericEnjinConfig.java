package com.enjin.common.config;

import com.enjin.core.config.EnjinConfig;
import com.enjin.core.config.JsonConfig;
import com.google.gson.annotations.SerializedName;
import lombok.Getter;
import lombok.Setter;

public class GenericEnjinConfig extends JsonConfig implements EnjinConfig {
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
