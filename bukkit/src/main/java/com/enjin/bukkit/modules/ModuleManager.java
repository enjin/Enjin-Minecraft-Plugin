package com.enjin.bukkit.modules;

import com.enjin.bukkit.util.Plugins;
import com.enjin.core.Enjin;
import com.enjin.core.EnjinPlugin;
import lombok.Getter;
import org.reflections.Reflections;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class ModuleManager {
    @Getter
    private EnjinPlugin        plugin;
    private Map<Class, Object> modules;

    public ModuleManager(EnjinPlugin plugin) {
        this.plugin = plugin;
        this.modules = new HashMap<>();
    }

    public void init() {
        Reflections   reflections = new Reflections("com.enjin.bukkit.modules.impl");
        Reflections.log = null;
        Set<Class<?>> modules     = reflections.getTypesAnnotatedWith(Module.class);

        if (modules != null && !modules.isEmpty()) {
            for (Class<?> clazz : modules) {
                Module module = clazz.getAnnotation(Module.class);
                if (module != null) {
                    boolean passedRequired = true;
                    for (String dep : module.hardPluginDependencies()) {
                        if (!Plugins.isEnabled(dep)) {
                            passedRequired = false;
                            break;
                        }
                    }

                    if (passedRequired) {
                        try {
                            Object instance = clazz.newInstance();
                            this.modules.put(clazz, instance);
                            Enjin.getLogger().debug("Module Initialized: " + module.name());
                        } catch (Exception e) {
                            Enjin.getLogger().log(e);
                        }
                    }
                }
            }
        }
    }

    public <T> T getModule(Class<T> type) {
        return modules.containsKey(type) ? type.cast(modules.get(type)) : null;
    }
}
