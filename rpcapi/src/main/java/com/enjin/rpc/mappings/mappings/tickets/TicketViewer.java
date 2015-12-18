package com.enjin.rpc.mappings.mappings.tickets;

import com.google.gson.annotations.SerializedName;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@ToString
@EqualsAndHashCode
public class TicketViewer {
    @Getter
    @SerializedName(value = "user_id")
    private Integer id;
    @Getter
    @SerializedName(value = "time_expire")
    private Long expiration;
}
