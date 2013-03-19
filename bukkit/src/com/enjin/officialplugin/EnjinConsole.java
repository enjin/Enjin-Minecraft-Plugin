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

        /*
        String[] text = new String[]{
            " _____             _   _         ",
            "|     ___| ___   |_| |_|  ___ ",
            "|     ___| |       |  |  | |  |  |      | ",
            "|_____| |_ |_|_|  | |_| |_ |_| ",
            "                      |__/              "
        };
        */
        /*
        String[] text = new String[]{
            " _____         _  _      ",
            "|   __| ___   |_||_| ___ ",
            "|   __||   |  | || ||   |",
            "|_____||_|_| _| ||_||_|_|",
            "            |___|        "
        };
        */
        return text;
//        return ChatColor.WHITE + "|----------------------" + ChatColor.LIGHT_PURPLE + " BUYCRAFT " + ChatColor.WHITE + "---------------------";
    }
}
