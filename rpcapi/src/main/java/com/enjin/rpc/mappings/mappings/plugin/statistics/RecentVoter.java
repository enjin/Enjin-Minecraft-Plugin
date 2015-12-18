package com.enjin.rpc.mappings.mappings.plugin.statistics;

import com.google.gson.annotations.SerializedName;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@ToString
@EqualsAndHashCode
@NoArgsConstructor
public class RecentVoter {
    @Getter
    @SerializedName(value = "player_name")
    private String name;
    @Getter
    @SerializedName(value = "vote_time")
    private Long time;
    @Getter
    @SerializedName(value = "list_id")
    private Integer listId;
    @Getter
    @SerializedName(value = "list_name")
    private String listName;
}
