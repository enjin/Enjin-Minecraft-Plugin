package com.enjin.rpc.mappings.mappings.minecraft;

import com.google.gson.annotations.SerializedName;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@NoArgsConstructor
@ToString
@EqualsAndHashCode
public class ServerInfo {
    @Getter
    @SerializedName(value = "server_id")
    private Integer id;
    @Getter
    private String host;
    @Getter
    private Integer port;
    @Getter
    private String name;
    @Getter
    private String description;
    @Getter
    private String version;
    @Getter
    private String features;
    @Getter
    private Long uptime;
    @Getter
    @SerializedName(value = "max_players")
    private Integer maxPlayers;
    @Getter
    @SerializedName(value = "players_online")
    private Integer playersOnline;
    @Getter
    @SerializedName(value = "pluginversion")
    private String pluginVersion;
    @Getter
    @SerializedName(value = "server_plugins")
    private String plugins;
    @Getter
    private Long updated;
    @Getter
    @SerializedName(value = "server_type")
    private String type;
}
