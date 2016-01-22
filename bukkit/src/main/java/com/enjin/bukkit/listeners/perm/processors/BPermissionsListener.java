package com.enjin.bukkit.listeners.perm.processors;

import com.enjin.bukkit.listeners.perm.PermissionListener;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.concurrent.ConcurrentHashMap;

public class BPermissionsListener extends PermissionListener {
    private ConcurrentHashMap<String, String> usereditingwhatplayer = new ConcurrentHashMap<String, String>();

    public void processCommand(CommandSender sender, String command, Event event) {
        //Make sure the user has permissions to run the value, otherwise we are just wasting time...
        if (sender.hasPermission("bPermissions.admin") && command.toLowerCase().startsWith("user ")) {
            String[] args = command.split(" ");
            update(sender, args);
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

    private void update(CommandSender sender, String[] args) {
        if (args.length > 2 && (args[1].trim().equalsIgnoreCase("addgroup") || args[1].trim().equalsIgnoreCase("rmgroup") || args[1].trim().equalsIgnoreCase("setgroup"))) {
            if (usereditingwhatplayer.containsKey(sender.getName())) {
                String ep = usereditingwhatplayer.get(sender.getName());
                OfflinePlayer op = Bukkit.getOfflinePlayer(ep);
                //We need to make sure the value executes before we actually grab the data.
                update(op);
            }
        } else if (args.length == 2) {
            usereditingwhatplayer.put(sender.getName(), args[1].trim());
        }
    }
}
