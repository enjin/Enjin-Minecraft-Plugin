package com.enjin.officialplugin.utils;

import com.enjin.officialplugin.EnjinMinecraftPlugin;
import org.spongepowered.api.text.Texts;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.util.command.CommandSource;

public class KeyVerifier implements Runnable {
    private EnjinMinecraftPlugin plugin;
    private CommandSource source;
    private String key;

    public KeyVerifier(String key, CommandSource source) {
        this.plugin = EnjinMinecraftPlugin.getInstance();
        this.source = source;
        this.key = key;
    }

    @Override
    public synchronized void run() {
        if (key.equals(plugin.getAuthKey())) {
            source.sendMessage(Texts.builder("The specified key and the existing one are the same!").color(TextColors.RED).build());
            return;
        }

        int validation = keyValid(true, key);
        if (validation == 0) {
            source.sendMessage(Texts.builder("That key is invalid! Make sure you've entered it properly!").color(TextColors.RED).build());
            // TODO: Stop tasks
            return;
        } else if (validation == 2) {
            // TODO: Stop tasks
            source.sendMessage(Texts.builder("There was a problem connecting to Enjin, please try again in a few minutes.").color(TextColors.RED).build());
            return;
        }

        plugin.getConfig().setAuthkey(key);
        plugin.saveConfig();

        // TODO: Stop then Start tasks and register events
        source.sendMessage(Texts.builder("Set the enjin key to " + key).color(TextColors.GREEN).build());
    }

    private int keyValid(boolean save, String key) {
        if (plugin.getConfig().isHttps() && !ConnectionUtil.testHTTPSconnection()) {
            plugin.getConfig().setHttps(false);
            plugin.getLogger().warn("SSL test connection failed, The plugin will use http without SSL. This may be less secure.");
        }

        try {
            if (key == null || key.length() < 2) {
                return 0;
            }

            if (save) {
                return EnjinAPI.sendAPIQuery("minecraft-auth", "key=" + key, "port=" + plugin.getPort(), "save=1");
            } else {
                return EnjinAPI.sendAPIQuery("minecraft-auth", "key=" + key, "port=" + plugin.getPort());
            }
        } catch (Throwable t) {
            plugin.getLogger().warn("There was an error synchronizing game data to the enjin server.");
            return 2;
        }
    }

}
