package com.enjin.rpc.mappings.deserializers;

import com.enjin.rpc.mappings.mappings.tickets.ExtraQuestion;
import com.google.gson.*;
import com.enjin.rpc.EnjinRPC;

import java.lang.reflect.Type;

public class ExtraQuestionDeserializer implements JsonDeserializer<ExtraQuestion> {
    @Override
    public ExtraQuestion deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        JsonObject object = jsonElement.getAsJsonObject();
        if (object.has("answer")) {
            JsonElement element = object.get("answer");
            if (!(element instanceof JsonNull)) {
                if (element.isJsonArray()) {
                    object.add("answer", element.getAsJsonArray());
                } else {
                    object.add("answer", element.getAsJsonPrimitive());
                }
            }
        }

        return EnjinRPC.gson.fromJson(jsonElement, type);
    }
}
