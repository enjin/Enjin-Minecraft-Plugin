package com.enjin.rpc.mappings.mappings.plugin;

import com.google.gson.annotations.SerializedName;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@ToString
@EqualsAndHashCode
@NoArgsConstructor
public class Auth {

    @Getter
    @SerializedName("authed")
    private boolean authed;
    @Getter
    @SerializedName("server_id")
    private long    serverId;

    public Auth(boolean authed, long serverId) {
        this.authed = authed;
        this.serverId = serverId;
    }

    public Auth(boolean authed) {
        this(authed, -1);
    }

}
