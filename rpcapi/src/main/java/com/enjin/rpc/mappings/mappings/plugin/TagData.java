package com.enjin.rpc.mappings.mappings.plugin;

import com.google.gson.annotations.SerializedName;
import lombok.Getter;

public class TagData {
    @Getter
    @SerializedName(value = "tag_id")
    private int id;
    @Getter
    @SerializedName(value = "tagname")
    private String name;
    @Getter
    @SerializedName(value = "date_added")
    private long date;
    @Getter
    @SerializedName(value = "expiry_time")
    private long expire;
}
