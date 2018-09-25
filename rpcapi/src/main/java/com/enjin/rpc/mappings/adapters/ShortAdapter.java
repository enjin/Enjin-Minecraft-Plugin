package com.enjin.rpc.mappings.adapters;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;

public class ShortAdapter extends TypeAdapter<Short> {
    @Override
    public void write(JsonWriter out, Short value) throws IOException {
        if (value == null) {
            out.nullValue();
            return;
        }

        out.value(value);
    }

    @Override
    public Short read(JsonReader in) throws IOException {
        if (in.peek() == JsonToken.NULL) {
            in.nextNull();
            return null;
        }

        try {
            String value = in.nextString();
            return Short.parseShort(value);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
