package com.enjin.rpc.mappings.mappings.minecraft;

import com.google.gson.annotations.SerializedName;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class ServerInfo {
    @Getter
    @SerializedName(value = "server_id")
    private int id;
    @Getter
    private String host;
    @Getter
    private int port;
    @Getter
    private String name;
    @Getter
    private String description;
    @Getter
    private String version;
    @Getter
    private String features;
    @Getter
    private long uptime;
    @Getter
    @SerializedName(value = "max_players")
    private int maxPlayers;
    @Getter
    @SerializedName(value = "players_online")
    private int playersOnline;
    @Getter
    @SerializedName(value = "pluginversion")
    private String pluginVersion;
    @Getter
    @SerializedName(value = "server_plugins")
    private String plugins;
    @Getter
    private long updated;
    @Getter
    @SerializedName(value = "server_type")
    private String type;
}
