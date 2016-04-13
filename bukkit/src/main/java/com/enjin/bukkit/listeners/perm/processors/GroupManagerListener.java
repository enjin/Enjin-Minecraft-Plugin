package com.enjin.bukkit.listeners.perm.processors;

import com.enjin.bukkit.listeners.perm.PermissionListener;
import com.enjin.bukkit.managers.VaultManager;
import net.milkbowl.vault.permission.Permission;
import org.anjocaido.groupmanager.events.GMUserEvent;
import org.anjocaido.groupmanager.events.GMUserEvent.Action;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GroupManagerListener extends PermissionListener {
	private Pattern pattern = Pattern.compile("^(?:mandemote|manpromote|manuadd|manudel|manuaddsub|manudelsub) ([a-zA-Z0-9]{2,16}) ([a-zA-Z0-9]{1,32})(?: ?)(?:[a-zA-Z0-9_]*)$");

    @Override
    public void processCommand(CommandSender sender, String command, Event event) {
        String[] parts = command.split(" ");
        if (parts.length == 2 && parts[0].equalsIgnoreCase("manudel")) {
			if (parts[1].length() >= 2 && parts[1].length() <= 16) {
				OfflinePlayer player = Bukkit.getOfflinePlayer(parts[1]);
				if (player != null) {
					update(player);
				}
			}
		} else if (parts.length > 2  && parts.length < 5 && parts[0].toLowerCase().startsWith("man")) {
			Matcher matcher = pattern.matcher(command);

			if (matcher != null && matcher.matches()) {
				if (VaultManager.isVaultEnabled() && VaultManager.isPermissionsAvailable()) {
					Permission permission = VaultManager.getPermission();
					if (groupExists(permission, parts[2])) {
						OfflinePlayer player = Bukkit.getOfflinePlayer(parts[1]);
						if (player != null) {
							update(player);
						}
					}
				}
			}
		}
    }

    private boolean groupExists(Permission permission, String group) {
        String[] groups = permission.getGroups();
        for (String g : groups) {
            if (group.equalsIgnoreCase(g)) {
                return true;
            }
        }
        return false;
    }
}
