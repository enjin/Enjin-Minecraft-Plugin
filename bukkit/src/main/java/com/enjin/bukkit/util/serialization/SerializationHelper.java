package com.enjin.bukkit.util.serialization;

import com.enjin.core.Enjin;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;

import java.util.Map;

public class SerializationHelper {

    private static final Gson GSON = new GsonBuilder().create();

    public static void validateAndPut(Object toSerialize, Map<String, Object> toStoreIn, String key) {
        try {
            JsonElement element = GSON.toJsonTree(toSerialize);
            toStoreIn.put(key, toSerialize);
        } catch (Exception ex) {
            Enjin.getLogger().log("Could not serialize object for key " + key, ex);
        }
    }

}
