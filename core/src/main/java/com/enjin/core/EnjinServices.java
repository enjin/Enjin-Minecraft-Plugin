package com.enjin.core;

import com.enjin.core.services.Service;

import java.util.HashMap;
import java.util.Map;

public class EnjinServices {
    private static final Map<Class<? extends Service>, Service> services = new HashMap<Class<? extends Service>, Service>();

    @SafeVarargs
    public static void registerServices(Class<? extends Service>... clazzes) {
        for (Class<? extends Service> clazz : clazzes) {
            try {
                Enjin.getLogger().debug("Registering service: " + clazz.getSimpleName());
                services.put(clazz, clazz.newInstance());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static <T extends Service> T getService(Class<T> clazz) {
        if (!services.containsKey(clazz)) {
            registerServices(clazz);
        }

        return clazz.cast(services.get(clazz));
    }
}
