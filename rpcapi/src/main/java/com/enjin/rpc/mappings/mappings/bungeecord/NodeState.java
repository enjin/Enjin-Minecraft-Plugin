package com.enjin.rpc.mappings.mappings.bungeecord;

import com.google.gson.annotations.SerializedName;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
public class NodeState {
    @Getter
    private List<String> players;
    @Getter
    @SerializedName(value = "maxplayers")
    private int maxPlayers;

    public NodeState(List<String> players, int maxPlayers) {
        this.players = players;
        this.maxPlayers = maxPlayers;
    }
}
