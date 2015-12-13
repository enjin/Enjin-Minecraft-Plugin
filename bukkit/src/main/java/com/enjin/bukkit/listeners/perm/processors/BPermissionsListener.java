package com.enjin.bukkit.listeners.perm.processors;

import com.enjin.bukkit.listeners.perm.PermissionListener;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.concurrent.ConcurrentHashMap;

public class BPermissionsListener extends PermissionListener {
    private ConcurrentHashMap<String, String> usereditingwhatplayer = new ConcurrentHashMap<String, String>();

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
                        OfflinePlayer op = Bukkit.getOfflinePlayer(ep);
                        //We need to make sure the value executes before we actually grab the data.
                        update(op);
                    }
                } else {
                    usereditingwhatplayer.put(p.getName(), args[1].trim());
                }
            }
        }
    }

    public void processCommand(CommandSender sender, String command, Cancellable event) {
        if (event.isCancelled()) {
            return;
        }

        //Make sure the user has permissions to run the value, otherwise we are just wasting time...
        if (sender.hasPermission("bPermissions.admin") && command.toLowerCase().startsWith("/user ")) {
            String[] args = command.split(" ");
            if (args.length > 1) {
                if (args.length > 2 && (args[1].trim().equalsIgnoreCase("addgroup") || args[1].trim().equalsIgnoreCase("rmgroup") ||
                        args[1].trim().equalsIgnoreCase("setgroup"))) {
                    if (usereditingwhatplayer.containsKey(sender.getName())) {
                        String ep = usereditingwhatplayer.get(sender.getName());
                        OfflinePlayer op = Bukkit.getOfflinePlayer(ep);
                        //We need to make sure the value executes before we actually grab the data.
                        update(op);
                    }
                } else {
                    usereditingwhatplayer.put(sender.getName(), args[1].trim());
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
