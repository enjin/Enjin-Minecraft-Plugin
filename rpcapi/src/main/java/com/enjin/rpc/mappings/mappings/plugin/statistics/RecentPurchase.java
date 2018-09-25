package com.enjin.rpc.mappings.mappings.plugin.statistics;

import com.google.gson.annotations.SerializedName;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.List;

@ToString
@EqualsAndHashCode
@NoArgsConstructor
public class RecentPurchase {
    @Getter
    @SerializedName(value = "player_name")
    private String        name;
    @Getter
    private List<Integer> items;
    @Getter
    private Double        price;
}
