package com.enjin.rpc.mappings.adapters;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import java.io.IOException;

public class ByteAdapter extends TypeAdapter<Byte> {
    @Override
    public void write(JsonWriter out, Byte value) throws IOException {
        if (value == null) {
            out.nullValue();
            return;
        }

        out.value(value);
    }

    @Override
    public Byte read(JsonReader in) throws IOException {
        if (in.peek() == JsonToken.NULL) {
            in.nextNull();
            return null;
        }

        try {
            String value = in.nextString();
            return Byte.parseByte(value);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
