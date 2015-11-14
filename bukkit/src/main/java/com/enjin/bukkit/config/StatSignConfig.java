package com.enjin.bukkit.config;

import com.enjin.bukkit.statsigns.SignData;
import com.enjin.core.config.JsonConfig;
import com.google.common.collect.Lists;
import lombok.Getter;

import java.util.List;

public class StatSignConfig extends JsonConfig {
    @Getter
    private List<SignData> heads = Lists.newArrayList();
}
