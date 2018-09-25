package com.enjin.rpc.mappings.mappings.plugin.data;

import com.google.gson.annotations.SerializedName;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class ClearInGameCacheData {
    @Getter
    private String  player;
    @Getter
    @SerializedName(value = "item_id")
    private Integer itemId;
    @Getter
    @SerializedName(value = "item_price")
    private String  itemPrice;
}
