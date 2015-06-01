package com.enjin.rpc.mappings.deserializers;

import com.enjin.rpc.mappings.mappings.tickets.ExtraQuestion;
import com.enjin.rpc.mappings.mappings.tickets.Ticket;
import com.enjin.rpc.mappings.mappings.tickets.TicketViewer;
import com.enjin.rpc.mappings.services.TicketsService;
import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import erpc.EnjinRPC;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class TicketDeserializer implements JsonDeserializer<Ticket> {
    @Override
    public Ticket deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        JsonObject object = jsonElement.getAsJsonObject();
        if (object.has("viewers")) {
            JsonElement element = object.get("viewers");
            if (!(element instanceof JsonNull)) {
                String json = element.getAsString();
                List<TicketViewer> viewers = EnjinRPC.gson.fromJson(json, new TypeToken<ArrayList<TicketViewer>>(){}.getType());
                object.add("viewers", EnjinRPC.gson.toJsonTree(viewers));
            } else {
                object.add("viewers", EnjinRPC.gson.toJsonTree(new ArrayList<Ticket>()));
            }
        }

        if (object.has("extra_questions")) {
            JsonElement element = object.get("extra_questions");
            if (!(element instanceof JsonNull)) {
                if (element.getAsString().equalsIgnoreCase("\"null\"")) {
                    object.add("extra_questions", EnjinRPC.gson.toJsonTree(new ArrayList<ExtraQuestion>()));
                } else {
                    String json = element.getAsString();
                    try {
                        System.out.println(element.toString());
                        List<ExtraQuestion> questions = TicketsService.GSON_EXTRA_QUESTION.fromJson(json, new TypeToken<ArrayList<ExtraQuestion>>(){}.getType());
                        object.add("extra_questions", EnjinRPC.gson.toJsonTree(questions));
                    } catch (Exception e) {
                        System.out.println(e.getMessage());
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
