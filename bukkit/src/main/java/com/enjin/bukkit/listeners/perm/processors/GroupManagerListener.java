package com.enjin.bukkit.listeners.perm.processors;

import com.enjin.bukkit.EnjinMinecraftPlugin;
import com.enjin.bukkit.listeners.perm.PermissionListener;
import com.enjin.bukkit.modules.impl.VaultModule;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.event.Event;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GroupManagerListener extends PermissionListener {
    private Pattern pattern = Pattern.compile("^(?:mandemote|manpromote|manuadd|manudel|manuaddsub|manudelsub) ([a-zA-Z0-9]{2,16}) ([a-zA-Z0-9]{1,32})(?: ?)(?:[a-zA-Z0-9_]*)$");

    @Override
    public void processCommand(CommandSender sender, String command, Event event) {
        VaultModule module = EnjinMinecraftPlugin.getInstance().getModuleManager().getModule(VaultModule.class);
        if (module != null) {
            String[] parts = command.split(" ");
            if (parts.length == 2 && parts[0].equalsIgnoreCase("manudel")) {
                if (parts[1].length() >= 2 && parts[1].length() <= 16) {
                    OfflinePlayer player = Bukkit.getOfflinePlayer(parts[1]);
                    if (player != null) {
                        update(player);
                    }
                }
            } else if (parts.length > 2 && parts.length < 5 && parts[0].toLowerCase().startsWith("man")) {
                Matcher matcher = pattern.matcher(command);

                if (matcher != null && matcher.matches()) {
                    if (module.isPermissionsAvailable()) {
                        if (module.groupExists(parts[2])) {
                            OfflinePlayer player = Bukkit.getOfflinePlayer(parts[1]);
                            if (player != null) {
                                update(player);
                            }
                        }
                    }
                }
            }
        }
    }
}
