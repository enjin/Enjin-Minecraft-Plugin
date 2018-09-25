package com.enjin.rpc.mappings.deserializers;

import com.enjin.core.Enjin;
import com.enjin.rpc.EnjinRPC;
import com.enjin.rpc.mappings.mappings.tickets.Condition;
import com.enjin.rpc.mappings.mappings.tickets.MetaOptions;
import com.enjin.rpc.mappings.mappings.tickets.Question;
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

public class QuestionDeserializer implements JsonDeserializer<Question> {
    @Override
    public Question deserialize(JsonElement jsonElement,
                                Type type,
                                JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        JsonObject object = jsonElement.getAsJsonObject();

        if (object.has("conditions")) {
            JsonElement element = object.get("conditions");
            if (!(element instanceof JsonNull)) {
                if (element.getAsString().equalsIgnoreCase("\"null\"")) {
                    object.remove("conditions");
                } else {
                    String json = element.getAsString();
                    try {
                        List<Condition> conditions = EnjinRPC.gson.fromJson(json, new TypeToken<List<Condition>>() {
                        }.getType());
                        object.add("conditions", EnjinRPC.gson.toJsonTree(conditions));
                    } catch (Exception e) {
                        Enjin.getLogger().log(e);
                        return null;
                    }
                }
            }
        }

        if (object.has("options")) {
            JsonElement element = object.get("options");
            if (!(element instanceof JsonNull)) {
                if (element.getAsString().equalsIgnoreCase("\"null\"")) {
                    object.add("options", EnjinRPC.gson.toJsonTree(new ArrayList<String>()));
                } else {
                    String json = element.getAsString();
                    try {
                        List<String> options = EnjinRPC.gson.fromJson(json, new TypeToken<ArrayList<String>>() {
                        }.getType());
                        object.add("options", EnjinRPC.gson.toJsonTree(options));
                    } catch (Exception e) {
                        Enjin.getLogger().log(e);
                        return null;
                    }
                }
            } else {
                object.add("options", EnjinRPC.gson.toJsonTree(new ArrayList<String>()));
            }
        }

        if (object.has("other_options")) {
            JsonElement element = object.get("other_options");
            if (!(element instanceof JsonNull)) {
                if (element.getAsString().equalsIgnoreCase("\"null\"")) {
                    object.remove("other_options");
                } else {
                    String json = element.getAsString();
                    try {
                        MetaOptions options = EnjinRPC.gson.fromJson(json, new TypeToken<MetaOptions>() {
                        }.getType());
                        object.add("other_options", EnjinRPC.gson.toJsonTree(options));
                    } catch (Exception e) {
                        Enjin.getLogger().log(e);
                        return null;
                    }
                }
            }
        }

        return EnjinRPC.gson.fromJson(jsonElement, type);
    }
}