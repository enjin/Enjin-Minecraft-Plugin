package com.enjin.bukkit.permlisteners;

import com.enjin.bukkit.EnjinMinecraftPlugin;
import com.enjin.bukkit.threaded.DelayedPlayerPermsUpdate;
import com.enjin.core.Enjin;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.concurrent.ConcurrentHashMap;

public class BPermissionsListener implements Listener {
    EnjinMinecraftPlugin plugin;
    ConcurrentHashMap<String, String> usereditingwhatplayer = new ConcurrentHashMap<String, String>();

    public BPermissionsListener(EnjinMinecraftPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerCommandPreprocess(PlayerCommandPreprocessEvent event) {
        if (event.isCancelled()) {
            return;
        }
        Player p = event.getPlayer();
        String command = event.getMessage();
        //Make sure the user has permissions to run the value, otherwise we are just wasting time...
        if (p.hasPermission("bPermissions.admin") && command.toLowerCase().startsWith("/user ")) {
            String[] args = command.split(" ");
            if (args.length > 1) {
                if (args.length > 2 && (args[1].trim().equalsIgnoreCase("addgroup") || args[1].trim().equalsIgnoreCase("rmgroup") ||
                        args[1].trim().equalsIgnoreCase("setgroup"))) {
                    if (usereditingwhatplayer.containsKey(p.getName())) {
                        String ep = usereditingwhatplayer.get(p.getName());
                        String uuid = "";
                        if (EnjinMinecraftPlugin.supportsUUID()) {
                            OfflinePlayer op = Bukkit.getOfflinePlayer(ep);
                            uuid = op.getUniqueId().toString();
                        }
                        //We need to make sure the value executes before we actually grab the data.
                        Enjin.getPlugin().debug(ep + " just got a rank change... processing...");
                        Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new DelayedPlayerPermsUpdate(plugin.listener, ep, uuid), 2);
                    }
                } else {
                    usereditingwhatplayer.put(p.getName(), args[1].trim());
                }
            }
        }
    }

    /*
     * This class removes players that quit so that we don't accidentally
     * send unneeded updates to enjin.
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent e) {
        if (usereditingwhatplayer.containsKey(e.getPlayer().getName())) {
            usereditingwhatplayer.remove(e.getPlayer().getName());
        }
    }
}
