package com.enjin.bukkit.managers;

import com.enjin.bukkit.EnjinMinecraftPlugin;
import com.enjin.bukkit.listeners.perm.processors.PermissionsBukkitListener;
import com.enjin.core.Enjin;
import lombok.Getter;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.json.simple.JSONObject;

import java.util.*;

public class VaultManager {
    @Getter
    private static Permission permission = null;
    @Getter
    private static Economy economy = null;
    private static boolean economyCompatibilityMode = false;

    public static void init(EnjinMinecraftPlugin plugin) {
        if (isVaultEnabled()) {
            initPermissions(plugin);
            initEconomy(plugin);
        } else {
			Enjin.getLogger().warning("Couldn't find the vault plugin! Please get it from dev.bukkit.org/bukkit-plugins/vault/!");
		}
    }

    private static void initEconomy(final EnjinMinecraftPlugin plugin) {
        RegisteredServiceProvider<Economy> provider = Bukkit.getServicesManager().getRegistration(Economy.class);
        if (provider != null) {
            economy = provider.getProvider();
            Bukkit.getScheduler().runTaskAsynchronously(plugin, new Runnable() {
                @Override
                public void run() {
                    try {
                        economy.hasAccount(Bukkit.getOfflinePlayer("Tux2"));
                    } catch (AbstractMethodError e) {
                        economyCompatibilityMode = true;
                        Enjin.getLogger().warning("Your economy plugin does not support UUID, using vault legacy compatibility mode.");
                        plugin.getLogger().warning("Your economy plugin does not support UUID, using vault legacy compatibility mode.");
                    }
                }
            });
        }
    }

    private static void initPermissions(EnjinMinecraftPlugin plugin) {
        RegisteredServiceProvider<Permission> provider = Bukkit.getServicesManager().getRegistration(Permission.class);
        if (provider == null || provider.getProvider() == null) {
            Enjin.getLogger().warning("Couldn't find a vault compatible permission plugin! Please install one before using the Enjin Minecraft Plugin.");
            Bukkit.getLogger().warning("[Enjin Minecraft Plugin] Couldn't find a vault compatible permission plugin! Please install one before using the Enjin Minecraft Plugin.");
            return;
        }
        permission = provider.getProvider();
    }

    public static boolean isVaultEnabled() {
        return Bukkit.getPluginManager().isPluginEnabled("Vault");
    }

    public static boolean isPermissionsAvailable() {
        return permission != null;
    }

    public static boolean isEconomyAvailable() {
        return economy != null;
    }

    public static boolean isEconomyUpToDate() {
        return !economyCompatibilityMode;
    }

	public static void setEconomyStats(String uuid, String name, JSONObject stats) {
		if (isEconomyAvailable()) {
			if (VaultManager.isEconomyUpToDate()) {
				OfflinePlayer player = null;

				try {
					player = Bukkit.getOfflinePlayer(UUID.fromString(uuid));
				} catch (IllegalArgumentException ignored) {

				}

				if (player == null || player.getName() == null || player.getName().equals("")) {
					player = Bukkit.getOfflinePlayer(name);
				}

				try {
					if (economy.hasAccount(player)) {
						stats.put("moneyamount", economy.getBalance(player));
					}
				} catch (Exception ignored) {}
			} else {
				try {
					if (economy.hasAccount(name)) {
						stats.put("moneyamount", economy.getBalance(name));
					}
				} catch (Exception ignored) {}
			}
		}
	}

	public static boolean groupExists(String group) {
		if (isPermissionsAvailable()) {
			String[] groups = permission.getGroups();
			for (String g : groups) {
				if (group.equalsIgnoreCase(g)) {
					return true;
				}
			}
		}

		return false;
	}

	public static Map<String, List<String>> getPlayerGroups(OfflinePlayer player) {
		Map<String, List<String>> groups = new HashMap<>();

		if (VaultManager.isVaultEnabled() && VaultManager.isPermissionsAvailable()) {
			if (permission.hasGroupSupport()) {
				String[] g = permission.getPlayerGroups(null, player);

				if (g == null || g.length == 0) {
					if (Bukkit.getPluginManager().isPluginEnabled("PermissionsBukkit")) {
						g = PermissionsBukkitListener.getGroups(player);
					}
				}

				if (g != null) {
					if (g.length > 0) {
						groups.put("*", Arrays.asList(g));
					}
				}

				for (World world : Bukkit.getWorlds()) {
					g = permission.getPlayerGroups(world.getName(), player);
					if (g != null && g.length > 0) {
						groups.put(world.getName(), Arrays.asList(g));
					}
				}
			}
		}

		return groups;
	}
}
