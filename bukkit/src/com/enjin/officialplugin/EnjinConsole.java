package com.enjin.officialplugin;

import org.bukkit.ChatColor;

/**
 * Handling misc chat/console functions
 */
public class EnjinConsole {
    public static String[] header()
    {
        String[] text = new String[]{
            ChatColor.GREEN + "=== Enjin Minecraft Plugin ==="
        };

        return text;
    }
}
