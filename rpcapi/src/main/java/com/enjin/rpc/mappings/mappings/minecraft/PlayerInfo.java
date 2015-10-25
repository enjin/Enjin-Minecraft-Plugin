package com.enjin.rpc.mappings.mappings.minecraft;

import com.google.gson.annotations.SerializedName;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@NoArgsConstructor
@ToString
@EqualsAndHashCode
public class PlayerInfo {
    @Getter
    private String name;
    @Getter
    private Boolean online;
    @Getter
    private int playtime;
    @Getter
    @SerializedName(value = "playtime_week")
    private int playtimeWeek;
    @Getter
    @SerializedName(value = "playtime_month")
    private int playtimeMonth;
    @Getter
    @SerializedName(value = "playtime_year")
    private int playtimeYear;
    @Getter
    @SerializedName(value = "playtime_alltime")
    private int playtimeAllTime;
    @Getter
    private String uuid;
    @Getter
    private Boolean vanished;
}
