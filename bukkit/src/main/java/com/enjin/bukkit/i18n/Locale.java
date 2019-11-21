package com.enjin.bukkit.i18n;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public enum Locale {

    en_US(StandardCharsets.UTF_8);

    private Charset charset;

    Locale(Charset charset) {
        this.charset = charset;
    }

    public String locale() {
        return name();
    }

    public Charset charset() {
        return charset;
    }

    public static Locale of(String name) {
        for (Locale locale : values()) {
            if (locale.name().equalsIgnoreCase(name))
                return locale;
        }

        return en_US;
    }

    public YamlConfiguration loadLocaleResource(Plugin plugin) {
        InputStream is = plugin.getResource(String.format("lang/%s.yml", name()));

        if (is == null)
            return null;

        return YamlConfiguration.loadConfiguration(new InputStreamReader(is, charset()));
    }
}
