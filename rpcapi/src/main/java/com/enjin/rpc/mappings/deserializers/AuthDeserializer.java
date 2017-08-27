package com.enjin.rpc.mappings.deserializers;

import com.enjin.rpc.mappings.mappings.plugin.Auth;
import com.google.gson.*;

import java.lang.reflect.Type;

public class AuthDeserializer implements JsonDeserializer<Auth> {
    @Override
    public Auth deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        boolean authed = false;
        long serverId = -1;
        if (jsonElement.isJsonPrimitive()) {
            authed = jsonElement.getAsBoolean();
        } else if (jsonElement.isJsonObject()) {
            JsonObject object = jsonElement.getAsJsonObject();
            if (object.has("authed")) {
                authed = object.get("authed").getAsBoolean();
            }
            if (object.has("server_id")) {
                serverId = object.get("server_id").getAsLong();
            }
        }
        return new Auth(authed, serverId);
    }
}
