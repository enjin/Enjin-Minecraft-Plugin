package com.enjin.rpc.mappings.mappings.shop;

import com.google.gson.annotations.SerializedName;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@ToString(callSuper = true)
@EqualsAndHashCode
public class FilteredItem extends Item {
    @Getter
    @SerializedName(value = "preset_id")
    private Integer preset;
}
