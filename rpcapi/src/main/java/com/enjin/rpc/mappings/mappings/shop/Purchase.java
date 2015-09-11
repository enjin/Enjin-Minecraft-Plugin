package com.enjin.rpc.mappings.mappings.shop;

import com.google.gson.annotations.SerializedName;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@ToString
@EqualsAndHashCode
public class Purchase {
    @Getter
    @SerializedName(value = "item_id")
    private int id;
    @Getter
    @SerializedName(value = "item_name")
    private String name;
    @Getter
    @SerializedName(value = "purchase_date")
    private String date;
    @Getter
    private String expires;
}
