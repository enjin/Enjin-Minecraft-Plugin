package com.enjin.bukkit.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class PlayerUtil {

    /**
     * Returns an OfflinePlayer instance for the provided player name if found.
     *
     * @param name the player name
     * @param cacheOnly search cache only if true, else use method that may block
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

}
