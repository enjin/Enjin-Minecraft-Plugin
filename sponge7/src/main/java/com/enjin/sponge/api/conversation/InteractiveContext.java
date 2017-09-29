package com.enjin.sponge.api.conversation;

import lombok.Getter;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.text.channel.ChatTypeMessageReceiver;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class InteractiveContext {
    private Object plugin;
    @Getter
    private ChatTypeMessageReceiver receiver;
    private Map<Object, Object> data;

    public InteractiveContext(Object plugin, ChatTypeMessageReceiver receiver) {
        this.plugin = plugin;
        this.receiver = receiver;
        this.data = new HashMap<>();
    }

    public Optional<PluginContainer> getPlugin() {
        return Sponge.getPluginManager().fromInstance(plugin);
    }

    public Object getData(Object key) {
        return data.get(key);
    }

    public void removeData(Object key) {
        data.remove(key);
    }

    public void setData(Object key, Object value) {
        data.put(key, value);
    }
}
