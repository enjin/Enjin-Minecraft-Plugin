package com.enjin.core;

import com.enjin.core.services.Service;

import java.util.HashMap;
import java.util.Map;

public class EnjinServices {
    private static final Map<Class<? extends Service>, Service> services = new HashMap<Class<? extends Service>, Service>();

    public static void registerServices(Class<? extends Service> ... clazzes) {
        for (Class<? extends Service> clazz : clazzes) {
            try {
                services.put(clazz, clazz.newInstance());
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
    }

    public static <T extends Service> T getService(Class<T> clazz) {
        return clazz.cast(services.get(clazz));
    }
}
