package com.enjin.core;

import lombok.Setter;

public class Enjin {
    @Setter
    private static EnjinPlugin plugin;

    public static EnjinPlugin getPlugin() {
        if (plugin == null) {
            plugin = () -> null;
        }

        return plugin;
    }
}
