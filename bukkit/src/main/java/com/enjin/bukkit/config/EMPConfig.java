package com.enjin.bukkit.config;

import com.enjin.common.config.GenericEnjinConfig;
import com.google.gson.annotations.SerializedName;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@ToString
public class EMPConfig extends GenericEnjinConfig {
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
}
