package com.enjin.bukkit.modules.impl;

import com.enjin.bukkit.EnjinMinecraftPlugin;
import com.enjin.bukkit.listeners.perm.processors.PermissionsBukkitListener;
import com.enjin.bukkit.modules.Module;
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

@Module(name = "Vault", hardPluginDependencies = {"Vault"})
public class VaultModule {
	private EnjinMinecraftPlugin plugin;
    @Getter
    private Permission permission = null;
    @Getter
    private Economy economy = null;
    private boolean economyCompatibilityMode = false;

	public VaultModule() {
		this.plugin = EnjinMinecraftPlugin.getInstance();
		init();
	}

    public void init() {
		initPermissions();
		initEconomy();
    }

	private void initPermissions() {
		RegisteredServiceProvider<Permission> provider = Bukkit.getServicesManager().getRegistration(Permission.class);
		if (provider != null && provider.getProvider() != null) {
			permission = provider.getProvider();
		} else {
			Enjin.getLogger().info("No Vault compatible permissions plugin was found. Vault permissions will be disabled.");
		}
	}

    private void initEconomy() {
        RegisteredServiceProvider<Economy> provider = Bukkit.getServicesManager().getRegistration(Economy.class);
        if (provider != null && provider.getProvider() != null) {
            economy = provider.getProvider();
            Bukkit.getScheduler().runTaskAsynchronously(plugin, new Runnable() {
                @Override
                public void run() {
                    try {
                        economy.hasAccount(Bukkit.getOfflinePlayer("Tux2"));
                    } catch (AbstractMethodError e) {
                        economyCompatibilityMode = true;
                        Enjin.getLogger().warning("Your economy plugin does not support UUID, using vault legacy compatibility mode.");
                    }
                }
            });
        } else {
			Enjin.getLogger().info("No Vault compatible economy plugin was found. Vault economy will be disabled.");
		}
    }

    public boolean isPermissionsAvailable() {
        return permission != null;
    }

    public boolean isEconomyAvailable() {
        return economy != null;
    }

    public boolean isEconomyUpToDate() {
        return !economyCompatibilityMode;
    }

	public void setEconomyStats(String uuid, String name, JSONObject stats) {
		if (isEconomyAvailable()) {
			if (isEconomyUpToDate()) {
				OfflinePlayer player = null;

				try {
					player = Bukkit.getOfflinePlayer(UUID.fromString(uuid));
				} catch (IllegalArgumentException e) {}

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

	public boolean groupExists(String group) {
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

	public Map<String, List<String>> getPlayerGroups(OfflinePlayer player) {
		Map<String, List<String>> groups = new HashMap<>();

		if (isPermissionsAvailable()) {
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
