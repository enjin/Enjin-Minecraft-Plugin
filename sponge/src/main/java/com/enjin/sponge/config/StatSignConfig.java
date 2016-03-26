package com.enjin.sponge.config;

import com.enjin.core.config.JsonConfig;
import com.enjin.sponge.statsigns.EnjinSignData;
import com.google.common.collect.Lists;
import lombok.Getter;

import java.util.List;

public class StatSignConfig extends JsonConfig {
    @Getter
    private List<EnjinSignData> signs = Lists.newArrayList();
}
