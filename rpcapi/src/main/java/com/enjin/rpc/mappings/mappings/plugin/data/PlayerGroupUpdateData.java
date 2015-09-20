package com.enjin.rpc.mappings.mappings.plugin.data;

import lombok.Getter;

public class PlayerGroupUpdateData {
    @Getter
    private String player;
    @Getter
    private String group;
    @Getter
    private String world = "*";
}
