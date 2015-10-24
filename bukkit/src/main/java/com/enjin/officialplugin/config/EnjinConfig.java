package com.enjin.officialplugin.config;

import com.enjin.core.config.JsonConfig;
import com.google.gson.annotations.SerializedName;
import lombok.Getter;
import lombok.Setter;

public class EnjinConfig extends JsonConfig {
    @Getter @Setter
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
    @SerializedName(value = "collect-player-stats")
    private boolean collectPlayerStats = true;
    @Getter @Setter
    @SerializedName(value = "send-stats-interval")
    private int sendStatsInterval = 5;
    @Getter @Setter
    @SerializedName(value = "listen-for-bans")
    private boolean listenForBans = true;
    @Getter
    @SerializedName(value = "stats-collected")
    private Stats statsCollected = new Stats();
    @Getter @Setter
    @SerializedName(value = "buy-command")
    private String buyCommand = "buy";
    @Getter @Setter
    @SerializedName(value = "use-buy-gui")
    private boolean useBuyGUI = true;
    @Getter @Setter
    @SerializedName(value = "logging-enabled")
    private boolean loggingEnabled = true;
    @Getter @Setter
    @SerializedName(value = "api-url")
    private String apiUrl = "://api.enjin.com/api";
}
