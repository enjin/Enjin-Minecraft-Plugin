package com.enjin.bukkit.config;

import com.enjin.core.config.JsonConfig;
import com.enjin.rpc.mappings.mappings.plugin.PlayerGroupInfo;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class RankUpdatesConfig extends JsonConfig {
    @Getter
    private Map<String, PlayerGroupInfo> playerPerms = new HashMap<>();
}
