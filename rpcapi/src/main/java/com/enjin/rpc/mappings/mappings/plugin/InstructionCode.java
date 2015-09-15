package com.enjin.rpc.mappings.mappings.plugin;

import com.enjin.rpc.mappings.mappings.plugin.data.ClearInGameCacheData;
import com.enjin.rpc.mappings.mappings.plugin.data.ExecuteData;
import lombok.Getter;

public enum InstructionCode {
    ADD_PLAYER_GROUP(null),
    REMOVE_PLAYER_GROUP(null),
    EXECUTE(ExecuteData.class),
    EXECUTE_AS(null),
    CONFIRMED_COMMANDS(null),
    CONFIG(null),
    ADD_PLAYER_WHITELIST(null),
    REMOVE_PLAYER_WHITELIST(null),
    RESPONSE_STATUS(null),
    BAN_PLAYER(null),
    UNBAN_PLAYER(null),
    CLEAR_INGAME_CACHE(ClearInGameCacheData.class),
    NOTIFICATIONS(null);

    @Getter
    private Class dataClass;

    InstructionCode(Class dataClass) {
        this.dataClass = dataClass;
    }
}
