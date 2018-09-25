package com.enjin.rpc.mappings.mappings.minecraft;

import com.google.gson.annotations.SerializedName;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@NoArgsConstructor
@ToString
@EqualsAndHashCode
public class MinecraftPlayerInfo {
    @Getter
    private String  name;
    @Getter
    private Boolean online;
    @Getter
    private Integer playtime;
    @Getter
    @SerializedName(value = "playtime_week")
    private Integer playtimeWeek;
    @Getter
    @SerializedName(value = "playtime_month")
    private Integer playtimeMonth;
    @Getter
    @SerializedName(value = "playtime_year")
    private Integer playtimeYear;
    @Getter
    @SerializedName(value = "playtime_alltime")
    private Integer playtimeAllTime;
    @Getter
    private String  uuid;
    @Getter
    private Boolean vanished;
}
