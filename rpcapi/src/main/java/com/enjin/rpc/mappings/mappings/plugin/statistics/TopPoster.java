package com.enjin.rpc.mappings.mappings.plugin.statistics;

import com.google.gson.annotations.SerializedName;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@ToString
@EqualsAndHashCode
@NoArgsConstructor
public class TopPoster {
    @Getter
    @SerializedName(value = "player_name")
    private String  name;
    @Getter
    private Integer posts;
}
