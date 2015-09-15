package com.enjin.rpc.mappings.deserializers;

import com.enjin.rpc.EnjinRPC;
import com.enjin.rpc.mappings.mappings.plugin.Instruction;
import com.enjin.rpc.mappings.mappings.plugin.InstructionCode;
import com.enjin.rpc.mappings.mappings.plugin.data.ExecuteData;
import com.google.gson.*;

import java.lang.reflect.Type;

public class InstructionDeserializer implements JsonDeserializer<Instruction> {
    @Override
    public Instruction deserialize(JsonElement element, Type type, JsonDeserializationContext context) throws JsonParseException {
        JsonObject object = element.getAsJsonObject();
        InstructionCode code = EnjinRPC.gson.fromJson(object.get("code"), InstructionCode.class);

        if (code != null) {
            switch (code) {
                case ADD_PLAYER_GROUP:
                    break;
                case REMOVE_PLAYER_GROUP:
                    break;
                case EXECUTE:
                    return new Instruction(code, EnjinRPC.gson.fromJson(object.get("data"), ExecuteData.class));
                case EXECUTE_AS:
                    break;
                case CONFIRMED_COMMANDS:
                    break;
                case CONFIG:
                    break;
                case ADD_PLAYER_WHITELIST:
                    break;
                case REMOVE_PLAYER_WHITELIST:
                    break;
                case RESPONSE_STATUS:
                    break;
                case BAN_PLAYER:
                    break;
                case UNBAN_PLAYER:
                    break;
                case CLEAR_INGAME_CACHE:
                    break;
                case NOTIFICATIONS:
                    break;
            }
        }

        return null;
    }
}
