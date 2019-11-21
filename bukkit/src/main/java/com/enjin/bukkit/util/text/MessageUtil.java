package com.enjin.bukkit.util.text;

import net.kyori.text.TextComponent;
import net.kyori.text.adapter.bukkit.TextAdapter;
import net.kyori.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class MessageUtil {

    private static final char TEX_FORMAT_TOKEN = '&';
    private static final LegacyComponentSerializer LEGACY_COMPONENT_SERIALIZER = LegacyComponentSerializer.INSTANCE;

    public static void sendComponent(CommandSender sender, TextComponent component) {
        if (sender == null || component == null)
            return;
        TextAdapter.sendComponent(sender, component);
    }

    public static void sendComponents(Player player, List<TextComponent> components) {
        for (TextComponent component : components)
            sendComponent(player, component);
    }

    public static void sendString(CommandSender sender, String message) {
        TextComponent component = LEGACY_COMPONENT_SERIALIZER.deserialize(message, TEX_FORMAT_TOKEN);
        sendComponent(sender, component);
    }

}
