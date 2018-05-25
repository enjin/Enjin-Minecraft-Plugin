package com.enjin.bukkit.config;

import com.enjin.bukkit.statsigns.SignData;
import com.enjin.core.config.JsonConfig;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

public class StatSignConfig extends JsonConfig {
    @Getter
    private List<SignData> signs = new ArrayList<>();
}
