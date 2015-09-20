package com.enjin.rpc.mappings.deserializers;

import com.enjin.rpc.EnjinRPC;
import com.enjin.rpc.mappings.mappings.plugin.Instruction;
import com.enjin.rpc.mappings.mappings.plugin.InstructionCode;
import com.google.gson.*;

import java.lang.reflect.Type;

public class InstructionDeserializer implements JsonDeserializer<Instruction> {
    @Override
    public Instruction deserialize(JsonElement element, Type type, JsonDeserializationContext context) throws JsonParseException {
        JsonObject object = element.getAsJsonObject();
        InstructionCode code = EnjinRPC.gson.fromJson(object.get("code"), InstructionCode.class);
        JsonElement data = object.get("data");

        if (code != null && code.getType() != null) {
            return new Instruction(code, EnjinRPC.gson.fromJson(data, code.getType()));
        }

        return null;
    }
}
