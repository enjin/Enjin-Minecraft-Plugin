package com.enjin.core.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class JsonConfig {
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public static <T extends JsonConfig> T load(File file, Class<T> clazz) {
        JsonConfig config;

        try {
            try {
                if (!file.exists()) {
                    config = clazz.newInstance();
                    config.save(file);
                } else {
                    config = gson.fromJson(new FileReader(file), clazz);
                }
            } catch (IOException e) {
                return clazz.newInstance();
            }
        } catch (ReflectiveOperationException e) {
            return null;
        }

        return config == null ? null : clazz.cast(config);
    }

    public boolean save(File file) {
        try {
            file.getParentFile().mkdirs();

            if (!file.exists()) {
                file.createNewFile();
            }

            FileWriter fw = new FileWriter(file);
            fw.write(gson.toJson(this));
            fw.close();
        } catch (IOException e) {
            return false;
        }

        return true;
    }
}