package com.enjin.rpc.mappings.deserializers;

import com.enjin.rpc.mappings.mappings.tickets.TicketResults;
import com.enjin.rpc.mappings.services.TicketService;
import com.google.gson.*;

import java.lang.reflect.Type;

public class TicketResultsDeserializer implements JsonDeserializer<TicketResults> {
    @Override
    public TicketResults deserialize(JsonElement element, Type type, JsonDeserializationContext context) throws JsonParseException {
        JsonObject result = element.getAsJsonObject();
        JsonObject results = result.getAsJsonObject("results");

        results.remove("replies_count");

        TicketResults output = TicketService.GSON_TICKET.fromJson(result, TicketResults.class);
        return output;
    }
}
