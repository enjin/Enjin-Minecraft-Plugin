package com.enjin.rpc.mappings.mappings.plugin;

import com.enjin.rpc.mappings.mappings.plugin.data.ClearInGameCacheData;
import com.enjin.rpc.mappings.mappings.plugin.data.ExecuteData;
import com.enjin.rpc.mappings.mappings.plugin.data.NotificationData;
import com.enjin.rpc.mappings.mappings.plugin.data.PlayerGroupUpdateData;
import com.google.gson.reflect.TypeToken;
import lombok.Getter;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;

public enum InstructionCode {
    ADD_PLAYER_GROUP(PlayerGroupUpdateData.class),
    REMOVE_PLAYER_GROUP(PlayerGroupUpdateData.class),
    EXECUTE(ExecuteData.class),
    EXECUTE_AS(null),
    CONFIRMED_COMMANDS(new TypeToken<ArrayList<Long>>(){}.getType()),
    CONFIG(new TypeToken<HashMap<String, Object>>(){}.getType()),
    ADD_PLAYER_WHITELIST(String.class),
    REMOVE_PLAYER_WHITELIST(String.class),
    RESPONSE_STATUS(String.class),
    BAN_PLAYER(String.class),
    UNBAN_PLAYER(null),
    CLEAR_INGAME_CACHE(ClearInGameCacheData.class),
    NOTIFICATIONS(NotificationData.class),
    PLUGIN_VERSION(String.class);

    @Getter
    private Type type;

    InstructionCode(Type type) {
        this.type = type;
    }
}
