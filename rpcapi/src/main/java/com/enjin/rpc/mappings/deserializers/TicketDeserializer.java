package com.enjin.rpc.mappings.deserializers;

import com.enjin.core.Enjin;
import com.enjin.rpc.EnjinRPC;
import com.enjin.rpc.mappings.mappings.tickets.ExtraQuestion;
import com.enjin.rpc.mappings.mappings.tickets.Ticket;
import com.enjin.rpc.mappings.services.TicketService;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class TicketDeserializer implements JsonDeserializer<Ticket> {
    @Override
    public Ticket deserialize(JsonElement jsonElement,
                              Type type,
                              JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        JsonObject object = jsonElement.getAsJsonObject();

        if (object.has("extra_questions")) {
            JsonElement element = object.get("extra_questions");
            if (!(element instanceof JsonNull)) {
                if (element.getAsString().equalsIgnoreCase("\"null\"")) {
                    object.add("extra_questions", EnjinRPC.gson.toJsonTree(new ArrayList<ExtraQuestion>()));
                } else {
                    String json = element.getAsString();
                    try {
                        List<ExtraQuestion> questions = TicketService.GSON_EXTRA_QUESTION.fromJson(json,
                                                                                                   new TypeToken<ArrayList<ExtraQuestion>>() {
                                                                                                   }.getType());
                        object.add("extra_questions", EnjinRPC.gson.toJsonTree(questions));
                    } catch (Exception e) {
                        Enjin.getLogger().warning(e.getMessage());
                        return null;
                    }
                }
            } else {
                object.add("extra_questions", EnjinRPC.gson.toJsonTree(new ArrayList<ExtraQuestion>()));
            }
        }

        return EnjinRPC.gson.fromJson(jsonElement, type);
    }
}
