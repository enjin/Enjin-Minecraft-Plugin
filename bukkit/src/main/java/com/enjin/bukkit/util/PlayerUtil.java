package com.enjin.bukkit.util;

import com.google.common.base.Optional;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.UUID;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class PlayerUtil {

    /**
     * Returns an OfflinePlayer instance for the provided player name if found.
     *
     * @param name      the player name
     * @param cacheOnly search cache only if true, else use method that may block
     *
     * @return OfflinePlayer instance or null
     */
    public static OfflinePlayer getOfflinePlayer(@NonNull String name, boolean cacheOnly) {
        OfflinePlayer player = null;
        if (cacheOnly) {
            for (OfflinePlayer p : Bukkit.getOfflinePlayers()) {
                if (p.getName() != null && p.getName().equalsIgnoreCase(name)) {
                    player = p;
                    break;
                }
            }
        } else {
            // May submit a blocking web request
            player = Bukkit.getOfflinePlayer(name);
        }
        return player;
    }

    public static Optional<OfflinePlayer> getOfflinePlayer(@NonNull UUID uuid, boolean cacheOnly) {
        Optional<OfflinePlayer> player = Optional.absent();

        if (cacheOnly) {
            for (OfflinePlayer p : Bukkit.getOfflinePlayers()) {
                if (p.getUniqueId() != null && p.getUniqueId().equals(uuid)) {
                    player = Optional.of(p);
                    break;
                }
            }
        } else {
            // May submit a block web request
            Optional.fromNullable(Bukkit.getOfflinePlayer(uuid));
        }

        return player;
    }

    public static Optional<Player> getPlayer(String name) {
        return Optional.fromNullable(Bukkit.getPlayer(name));
    }

    public static Optional<Player> getPlayer(UUID uuid) {
        return Optional.fromNullable(Bukkit.getPlayer(uuid));
    }

}
