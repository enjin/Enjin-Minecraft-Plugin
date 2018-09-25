package com.enjin.bukkit.config;

import com.google.gson.annotations.SerializedName;
import lombok.Getter;
import lombok.Setter;

public class PlayerStats {
    @Getter
    @Setter
    private boolean travel       = true;
    @Getter
    @Setter
    @SerializedName(value = "blocks-broken")
    private boolean blocksBroken = true;
    @Getter
    @Setter
    @SerializedName(value = "blocks-placed")
    private boolean blocksPlaced = true;
    @Getter
    @Setter
    private boolean kills        = true;
    @Getter
    @Setter
    private boolean deaths       = true;
    @Getter
    @Setter
    private boolean xp           = true;
}
