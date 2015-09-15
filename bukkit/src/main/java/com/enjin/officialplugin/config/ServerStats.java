package com.enjin.officialplugin.config;

import com.google.gson.annotations.SerializedName;
import lombok.Getter;
import lombok.Setter;

public class ServerStats {
    @Getter
    @Setter
    @SerializedName(value = "creeper-explosions")
    private boolean creeperExplosions = true;
    @Getter @Setter
    @SerializedName(value = "player-kicks")
    private boolean playerKicks = true;
}
