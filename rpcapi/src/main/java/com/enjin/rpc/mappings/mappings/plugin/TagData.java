package com.enjin.rpc.mappings.mappings.plugin;

import com.google.gson.annotations.SerializedName;
import lombok.Getter;

public class TagData {
    @Getter
    @SerializedName(value = "tag_id")
    private Integer id;
    @Getter
    @SerializedName(value = "tagname")
    private String  name;
    @Getter
    @SerializedName(value = "date_added")
    private Long    date;
    @Getter
    @SerializedName(value = "expiry_time")
    private Long    expire;
}
