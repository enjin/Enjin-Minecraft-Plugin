package com.enjin.core;

import com.enjin.core.config.EnjinConfig;
import com.enjin.core.util.EnjinLogger;
import lombok.Getter;
import lombok.Setter;

public class Enjin {
    @Setter
    private static EnjinPlugin plugin;
    @Getter @Setter
    private static EnjinLogger logger;
    @Getter @Setter
    private static EnjinConfig configuration;

    public static EnjinPlugin getPlugin() {
        return plugin;
    }

    public static <T extends EnjinConfig> T getConfiguration(Class<T> clazz) {
        return clazz.cast(configuration);
    }
}
