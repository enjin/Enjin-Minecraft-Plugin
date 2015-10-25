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
    private int maxPlayers;

    public NodeState(List<String> players, int maxPlayers) {
        this.players = players;
        this.maxPlayers = maxPlayers;
    }
}
