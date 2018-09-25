package com.enjin.bukkit.util.text;

import net.kyori.text.TextComponent;
import net.kyori.text.serializer.ComponentSerializers;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.chat.ComponentSerializer;
import org.bukkit.entity.Player;

import java.util.List;

public class MessageUtil {

    public static void sendMessage(Player player, TextComponent component) {
        String          json       = ComponentSerializers.JSON.serialize(component);
        BaseComponent[] components = ComponentSerializer.parse(json);
        player.spigot().sendMessage(components);
    }

    public static void sendMessages(Player player, List<TextComponent> components) {
        for (TextComponent component : components) {
            sendMessage(player, component);
        }
    }

}
