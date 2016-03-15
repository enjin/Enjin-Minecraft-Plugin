package com.enjin.sponge.config;

import com.enjin.common.config.GenericEnjinConfig;
import com.google.gson.annotations.SerializedName;
import lombok.Getter;
import lombok.Setter;

public class EMPConfig extends GenericEnjinConfig {
    @Getter @Setter
    @SerializedName(value = "buy-command")
    private String buyCommand = "buy";
    @Getter @Setter
    @SerializedName(value = "use-buy-gui")
    private boolean useBuyGUI = true;
	@Getter @Setter
	@SerializedName(value = "collect-player-stats")
	private boolean collectPlayerStats = true;
}
