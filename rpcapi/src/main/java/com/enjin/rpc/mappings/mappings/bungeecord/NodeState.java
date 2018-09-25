package com.enjin.rpc.mappings.mappings.bungeecord;

import com.google.gson.annotations.SerializedName;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.List;

@NoArgsConstructor
@ToString
@EqualsAndHashCode
public class NodeState {
    @Getter
    private List<String> players;
    @Getter
    @SerializedName(value = "maxplayers")
    private Integer      maxPlayers;

    public NodeState(List<String> players, Integer maxPlayers) {
        this.players = players;
        this.maxPlayers = maxPlayers;
    }
}
