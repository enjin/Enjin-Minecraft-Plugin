package com.enjin.bukkit.util;

import com.enjin.bukkit.EnjinMinecraftPlugin;
import com.enjin.bukkit.modules.ModuleManager;
import com.enjin.bukkit.modules.impl.VaultModule;
import org.bukkit.command.CommandSender;

public class PermissionsUtil {
    public static boolean hasPermission(CommandSender sender, String perm) {
        ModuleManager manager = EnjinMinecraftPlugin.getInstance().getModuleManager();
        if (manager != null) {
            VaultModule module = manager.getModule(VaultModule.class);
            if (module != null && module.isPermissionsAvailable()) {
                return module.getPermission().has(sender, perm);
            }
        }

        return sender.hasPermission(perm);
    }
}
