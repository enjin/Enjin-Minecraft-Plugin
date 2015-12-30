package com.enjin.bukkit.util;

import com.google.common.base.Optional;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.UUID;

public class OptionalUtil {
    public static Optional<Player> getPlayer(UUID uuid) {
        return Optional.fromNullable(Bukkit.getPlayer(uuid));
    }
}
