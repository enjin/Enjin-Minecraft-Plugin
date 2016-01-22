package com.enjin.bukkit.managers;

import com.enjin.bukkit.EnjinMinecraftPlugin;
import com.enjin.core.Enjin;
import lombok.Getter;
import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.Bukkit;
import org.bukkit.plugin.RegisteredServiceProvider;

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
        }
    }

    private static void initEconomy(final EnjinMinecraftPlugin plugin) {
        RegisteredServiceProvider<Economy> provider = Bukkit.getServicesManager().getRegistration(net.milkbowl.vault.economy.Economy.class);
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
}
