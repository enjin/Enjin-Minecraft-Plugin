package com.enjin.rpc.mappings.mappings.plugin.data;

import com.google.gson.annotations.SerializedName;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public class ClearInGameCacheData {
    @Getter
    private String player;
    @Getter
    @SerializedName(value = "item_id")
    private int itemId;
    @Getter
    @SerializedName(value = "item_price")
    private String itemPrice;
}
