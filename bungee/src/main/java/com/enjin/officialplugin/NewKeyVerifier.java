package com.enjin.officialplugin;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;

public class NewKeyVerifier implements Runnable {

    EnjinPlugin plugin;
    String key;
    CommandSender sender;
    ProxiedPlayer player = null;
    public boolean completed = false;
    public boolean pluginboot = true;

    public NewKeyVerifier(EnjinPlugin plugin, String key, CommandSender sender, boolean pluginboot) {
        this.plugin = plugin;
        this.key = key;
        this.sender = sender;
        if (sender instanceof ProxiedPlayer) {
            player = (ProxiedPlayer) sender;
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
                    plugin.logger.warning("[Enjin Minecraft Plugin] Unable to connect to the internet to verify your key! Please check your internet connection.");
                    EnjinPlugin.enjinlogger.warning("Unable to connect to the internet to verify your key! Please check your internet connection.");
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
                EnjinPlugin.debug("Key valid.");
                plugin.enableTasks();
                //plugin.registerEvents();
            } else if (validation == 0) {
                plugin.authkeyinvalid = true;
                plugin.logger.warning("[Enjin Minecraft Plugin] Invalid key! Please regenerate your key and try again.");
                EnjinPlugin.enjinlogger.warning("Invalid key! Please regenerate your key and try again.");
            } else {
                plugin.authkeyinvalid = true;
                plugin.logger.warning("[Enjin Minecraft Plugin] There was a problem connecting to Enjin, please try again in a few minutes. (If you continue to see this message, please type \"/enjin report\" and send the enjinreport_xxx.txt file to Enjin Support for further assistance.)");
                EnjinPlugin.enjinlogger.warning("There was a problem connecting to Enjin, please try again in a few minutes. (If you continue to see this message, please type \"/enjin report\" and send the enjinreport_xxx.txt file to Enjin Support for further assistance.)");
            }
            completed = true;
        } else {
            if (key.equals(EnjinPlugin.getHash())) {
                TextComponent message = new TextComponent("The specified key and the existing one are the same!");
                message.setColor(ChatColor.YELLOW);
                sender.sendMessage(message);
                completed = true;
                return;
            }
            int validation = keyValid(true, key);
            if (validation == 0) {
                TextComponent message = new TextComponent("That key is invalid! Make sure you've entered it properly!");
                message.setColor(ChatColor.RED);
                sender.sendMessage(message);
                plugin.stopTask();
                //plugin.unregisterEvents();
                completed = true;
                return;
            } else if (validation == 2) {
                TextComponent message = new TextComponent("There was a problem connecting to Enjin, please try again in a few minutes. (If you continue to see this message, please type \"/benjin report\" and send the enjinreport_xxx.txt file to Enjin Support for further assistance.)");
                message.setColor(ChatColor.RED);
                sender.sendMessage(message);
                plugin.stopTask();
                //plugin.unregisterEvents();
                completed = true;
                return;
            }
            plugin.authkeyinvalid = false;
            EnjinPlugin.setHash(key);
            EnjinPlugin.debug("Writing hash to file.");
            EnjinPlugin.config.set("authkey", key);
            plugin.saveConfig();
            TextComponent message = new TextComponent("Set the enjin key to " + key);
            message.setColor(ChatColor.GREEN);
            sender.sendMessage(message);
            plugin.stopTask();
            //plugin.unregisterEvents();
            plugin.enableTasks();
            //plugin.registerEvents();
            completed = true;
        }
        completed = true;
    }

    private int keyValid(boolean save, String key) {
        //No need to test the ssl connection if it is already false.
        if (EnjinPlugin.usingSSL && !plugin.testHTTPSconnection()) {
            EnjinPlugin.usingSSL = false;
            plugin.getLogger().warning("[Enjin Minecraft Plugin] SSL test connection failed, The plugin will use http without SSL. This may be less secure.");
            EnjinPlugin.enjinlogger.warning("SSL test connection failed, The plugin will use http without SSL. This may be less secure.");
        }
        try {
            if (key == null) {
                return 0;
            }
            if (key.length() < 2) {
                return 0;
            }
            if (save) {
                return EnjinPlugin.sendAPIQuery("minecraft-auth", "key=" + key, "port=" + plugin.minecraftport, "save=1"); //save
            } else {
                return EnjinPlugin.sendAPIQuery("minecraft-auth", "key=" + key, "port=" + plugin.minecraftport); //just check info
            }
        } catch (Throwable t) {
            plugin.logger.warning("[Enjin Minecraft Plugin] There was an error synchronizing game data to the enjin server.");
            t.printStackTrace();
            plugin.lasterror = new EnjinErrorReport(t, "Verifying key when error was thrown:");
            EnjinPlugin.enjinlogger.warning("There was an error synchronizing game data to the enjin server." + plugin.lasterror.toString());
            return 2;
        }
    }

}
