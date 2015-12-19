package com.enjin.rpc.mappings.deserializers;

import com.enjin.rpc.EnjinRPC;
import com.enjin.rpc.mappings.mappings.tickets.ReplyResults;
import com.google.gson.*;

import java.lang.reflect.Type;

public class ReplyResultsDeserializer implements JsonDeserializer<ReplyResults> {
    @Override
    public ReplyResults deserialize(JsonElement element, Type type, JsonDeserializationContext context) throws JsonParseException {
        JsonObject result = element.getAsJsonObject();
        JsonObject results = result.getAsJsonObject("results");

        results.remove("text");
        results.remove("username");

        ReplyResults output = EnjinRPC.gson.fromJson(result, ReplyResults.class);
        return output;
    }
}
