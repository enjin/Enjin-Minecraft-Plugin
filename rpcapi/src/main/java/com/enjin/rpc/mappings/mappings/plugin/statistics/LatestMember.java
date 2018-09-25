package com.enjin.rpc.mappings.mappings.plugin.statistics;

import com.google.gson.annotations.SerializedName;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@ToString
@EqualsAndHashCode
@NoArgsConstructor
public class LatestMember {
    @Getter
    @SerializedName(value = "player_name")
    private String name;
    @Getter
    @SerializedName(value = "datejoined")
    private Long   dateJoined;
}
