package com.enjin.rpc.mappings.mappings.plugin;

import com.google.gson.annotations.SerializedName;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.util.List;
import java.util.Map;

@ToString
@EqualsAndHashCode
@AllArgsConstructor
public class Status {
    @Getter
    @SerializedName(value = "java_version")
    private String                       javaVersion;
    @Getter
    @SerializedName(value = "mc_version")
    private String                       mcVersion;
    @Getter
    private List<String>                 plugins;
    @Getter
    @SerializedName(value = "hasranks")
    private Boolean                      ranksEnabled;
    @Getter
    @SerializedName(value = "pluginversion")
    private String                       pluginVersion;
    @Getter
    private List<String>                 worlds;
    @Getter
    private List<String>                 groups;
    @Getter
    @SerializedName(value = "maxplayers")
    private Integer                      maxPlayers;
    @Getter
    private Integer                      players;
    @Getter
    @SerializedName(value = "playerlist")
    private List<PlayerInfo>             playersList;
    @Getter
    @SerializedName(value = "playergroups")
    private Map<String, PlayerGroupInfo> playerGroups;
    @Getter
    private Double                       tps;
    @Getter
    @SerializedName(value = "executed_commands")
    private List<ExecutedCommand>        executedCommands;
    @Getter
    @SerializedName(value = "votifier")
    private Map<String, List<Object[]>>  votes;

    /**
     * gzipped and base64 encoded
     */
    @Getter
    private String stats;
}
