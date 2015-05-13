package com.enjin.officialplugin.permlisteners;

import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import com.enjin.officialplugin.EnjinMinecraftPlugin;
import com.enjin.officialplugin.threaded.DelayedPlayerPermsUpdate;


public class bPermsChangeListener implements Listener {

    EnjinMinecraftPlugin plugin;
    ConcurrentHashMap<String, String> usereditingwhatplayer = new ConcurrentHashMap<String, String>();

    public bPermsChangeListener(EnjinMinecraftPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void preCommandListener(PlayerCommandPreprocessEvent event) {
        if (event.isCancelled()) {
            return;
        }
        Player p = event.getPlayer();
        String command = event.getMessage();
        //Make sure the user has permissions to run the command, otherwise we are just wasting time...
        if (p.hasPermission("bPermissions.admin") && command.toLowerCase().startsWith("/user ")) {
            String[] args = command.split(" ");
            if (args.length > 1) {
                if (args.length > 2 && (args[1].trim().equalsIgnoreCase("addgroup") || args[1].trim().equalsIgnoreCase("rmgroup") ||
                        args[1].trim().equalsIgnoreCase("setgroup"))) {
                    if (usereditingwhatplayer.containsKey(p.getName())) {
                        String ep = usereditingwhatplayer.get(p.getName());
                        //We need to make sure the command executes before we actually grab the data.
                        EnjinMinecraftPlugin.debug(ep + " just got a rank change... processing...");
                        Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new DelayedPlayerPermsUpdate(plugin.listener, ep), 2);
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
