package com.enjin.core;

import com.enjin.core.config.EnjinConfig;
import lombok.Getter;
import lombok.Setter;

public class Enjin {
    @Setter
    private static EnjinPlugin plugin;
    @Getter @Setter
    private static EnjinConfig configuration;

    public static EnjinPlugin getPlugin() {
        if (plugin == null) {
            plugin = () -> null;
        }

        return plugin;
    }

    public static <T extends EnjinConfig> T getConfiguration(Class<T> clazz) {
        return clazz.cast(configuration);
    }
}
