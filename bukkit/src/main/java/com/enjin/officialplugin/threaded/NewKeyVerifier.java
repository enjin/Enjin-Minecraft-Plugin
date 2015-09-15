package com.enjin.officialplugin.threaded;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import com.enjin.officialplugin.EnjinErrorReport;
import com.enjin.officialplugin.EnjinMinecraftPlugin;

public class NewKeyVerifier implements Runnable {

    EnjinMinecraftPlugin plugin;
    String key;
    CommandSender sender;
    Player player = null;
    public boolean completed = false;
    public boolean pluginboot = true;

    public NewKeyVerifier(EnjinMinecraftPlugin plugin, String key, CommandSender sender, boolean pluginboot) {
        this.plugin = plugin;
        this.key = key;
        this.sender = sender;
        if (sender instanceof Player) {
            player = (Player) sender;
        }
        this.pluginboot = pluginboot;
    }

    @Override
    public synchronized void run() {

        if (pluginboot) {
            //Make sure we have an internet connection before we
            //validate the key.
            int i = 0;
            while (!plugin.testWebConnection()) {
                //Let's spit out a warning message every 5 minutes that the plugin is unable to contact enjin.
                if (++i > 5) {
                    Bukkit.getLogger().warning("[Enjin Minecraft Plugin] Unable to connect to the internet to verify your key! Please check your internet connection.");
                    EnjinMinecraftPlugin.enjinlogger.warning("Unable to connect to the internet to verify your key! Please check your internet connection.");
                    i = 0;
                }
                try {
                    //let's wait a minute before trying again
                    wait(60000);
                } catch (InterruptedException e) {
                    //e.printStackTrace();
                }
            }

            int validation = keyValid(false, key);
            if (validation == 1) {
                plugin.authkeyinvalid = false;
                EnjinMinecraftPlugin.debug("Key valid.");
                plugin.startTask();
                plugin.registerEvents();
            } else if (validation == 0) {
                plugin.authkeyinvalid = true;
                Bukkit.getLogger().warning("[Enjin Minecraft Plugin] Invalid key! Please regenerate your key and try again.");
                EnjinMinecraftPlugin.enjinlogger.warning("Invalid key! Please regenerate your key and try again.");
            } else {
                plugin.authkeyinvalid = true;
                Bukkit.getLogger().warning("[Enjin Minecraft Plugin] There was a problem connecting to Enjin, please try again in a few minutes. (If you continue to see this message, please type \"/enjin report\" and send the enjinreport_xxx.txt file to Enjin Support for further assistance.)");
                EnjinMinecraftPlugin.enjinlogger.warning("There was a problem connecting to Enjin, please try again in a few minutes. (If you continue to see this message, please type \"/enjin report\" and send the enjinreport_xxx.txt file to Enjin Support for further assistance.)");
            }
            completed = true;
        } else {
            if (key.equals(EnjinMinecraftPlugin.getAuthKey())) {
                if (player == null || player.isOnline()) {
                    sender.sendMessage(ChatColor.YELLOW + "The specified key and the existing one are the same!");
                }
                completed = true;
                return;
            }
            int validation = keyValid(true, key);
            if (validation == 0) {
                if (player == null || player.isOnline()) {
                    sender.sendMessage(ChatColor.RED + "That key is invalid! Make sure you've entered it properly!");
                }
                plugin.stopTask();
                plugin.unregisterEvents();
                completed = true;
                return;
            } else if (validation == 2) {
                if (player == null || player.isOnline()) {
                    sender.sendMessage(ChatColor.RED + "There was a problem connecting to Enjin, please try again in a few minutes. (If you continue to see this message, please type \"/enjin report\" and send the enjinreport_xxx.txt file to Enjin Support for further assistance.)");
                }
                plugin.stopTask();
                plugin.unregisterEvents();
                completed = true;
                return;
            }
            plugin.authkeyinvalid = false;
            EnjinMinecraftPlugin.setAuthKey(key);
            EnjinMinecraftPlugin.debug("Writing hash to file.");
            plugin.config.setAuthKey(key);
            plugin.saveConfig();
            if (player == null || player.isOnline()) {
                sender.sendMessage(ChatColor.GREEN + "Set the enjin key to " + key);
            }
            plugin.stopTask();
            plugin.unregisterEvents();
            plugin.startTask();
            plugin.registerEvents();
            completed = true;
        }
        completed = true;
    }

    private int keyValid(boolean save, String key) {
        //No need to test the ssl connection if it is already false.
        if (EnjinMinecraftPlugin.usingSSL && !plugin.testHTTPSconnection()) {
            EnjinMinecraftPlugin.usingSSL = false;
            plugin.getLogger().warning("[Enjin Minecraft Plugin] SSL test connection failed, The plugin will use http without SSL. This may be less secure.");
            EnjinMinecraftPlugin.enjinlogger.warning("SSL test connection failed, The plugin will use http without SSL. This may be less secure.");
        }
        try {
            if (key == null) {
                return 0;
            }
            if (key.length() < 2) {
                return 0;
            }
            if (save) {
                return EnjinMinecraftPlugin.sendAPIQuery("minecraft-auth", "key=" + key, "port=" + EnjinMinecraftPlugin.minecraftport, "save=1"); //save
            } else {
                return EnjinMinecraftPlugin.sendAPIQuery("minecraft-auth", "key=" + key, "port=" + EnjinMinecraftPlugin.minecraftport); //just check info
            }
        } catch (Throwable t) {
            Bukkit.getLogger().warning("[Enjin Minecraft Plugin] There was an error synchronizing game data to the enjin server.");
            t.printStackTrace();
            plugin.lasterror = new EnjinErrorReport(t, "Verifying key when error was thrown:");
            EnjinMinecraftPlugin.enjinlogger.warning("There was an error synchronizing game data to the enjin server." + plugin.lasterror.toString());
            return 2;
        }
    }

}
