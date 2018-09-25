package com.enjin.bukkit.command.commands;

import com.enjin.bukkit.EnjinMinecraftPlugin;
import com.enjin.bukkit.command.Directive;
import com.enjin.bukkit.command.Permission;
import com.enjin.bukkit.config.EMPConfig;
import com.enjin.core.Enjin;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

public class ConfigCommand {
    @Permission("enjin.config")
    @Directive(parent = "enjin", value = "config", aliases = "conf", requireValidKey = false)
    public static void config(CommandSender sender, String[] args) {
        if (args.length == 0) {
            help(sender);
        } else {
            String    rawKey = args[0];
            ConfigKey key    = null;
            for (ConfigKey k : ConfigKey.values()) {
                if (k.name().equalsIgnoreCase(rawKey)) {
                    key = k;
                }
            }

            if (key != null && args.length > 1) {
                process(sender, key, args[1]);
            } else {
                help(sender);
            }
        }
    }

    private static void help(CommandSender sender) {
        StringBuilder builder = new StringBuilder();
        for (ConfigKey key : ConfigKey.values()) {
            if (builder.length() != 0) {
                builder.append(", ");
            }

            builder.append(key);
        }
        sender.sendMessage(ChatColor.GOLD + "USAGE: /e config <key> <value>");
        sender.sendMessage(ChatColor.GREEN + "Keys: " + builder.toString());
    }

    private static void process(CommandSender sender, ConfigKey key, String rawValue) {
        Object value = null;
        try {
            if (key.type == Boolean.class) {
                if (rawValue.equalsIgnoreCase("true") || rawValue.equalsIgnoreCase("false")) {
                    value = Boolean.parseBoolean(rawValue);
                } else {
                    throw new Exception("Could not parse string to Boolean");
                }
            } else if (key.type == Integer.class) {
                value = Integer.parseInt(rawValue);
            }
        } catch (Exception e) {
            sender.sendMessage(ChatColor.RED + key.name() + " must be of type " + key.type.getSimpleName());
            return;
        }

        if (value == null) {
            sender.sendMessage(ChatColor.RED + "Was unable to process raw value for " + key.name());
            return;
        }

        boolean success = true;
        if (key == ConfigKey.DEBUG) {
            Enjin.getConfiguration().setDebug((Boolean) value);
            Enjin.getLogger().setDebug((Boolean) value);
        } else if (key == ConfigKey.HTTPS) {
            Enjin.getConfiguration().setHttps((Boolean) value);
        } else if (key == ConfigKey.LOGGING_ENABLED) {
            Enjin.getConfiguration().setLoggingEnabled((Boolean) value);
        } else if (key == ConfigKey.COLLECT_PLAYER_STATS) {
            Enjin.getConfiguration(EMPConfig.class).setCollectPlayerStats((Boolean) value);
        } else if (key == ConfigKey.SEND_STATS_INTERVAL) {
            Enjin.getConfiguration(EMPConfig.class).setSendStatsInterval((Integer) value);
        } else if (key == ConfigKey.USE_BUY_GUI) {
            Enjin.getConfiguration(EMPConfig.class).setUseBuyGUI((Boolean) value);
        } else {
            success = false;
            sender.sendMessage(ChatColor.RED + "Invalid or unsupported config change found. Unable to process config update.");
        }

        if (success) {
            EnjinMinecraftPlugin.saveConfiguration();
            sender.sendMessage(ChatColor.GREEN + key.name() + " was set to " + value);
        }
    }

    public enum ConfigKey {
        DEBUG(Boolean.class),
        HTTPS(Boolean.class),
        AUTO_UPDATE(Boolean.class),
        LOGGING_ENABLED(Boolean.class),
        COLLECT_PLAYER_STATS(Boolean.class),
        SEND_STATS_INTERVAL(Integer.class),
        USE_BUY_GUI(Boolean.class);

        private Class<?> type;

        ConfigKey(Class<?> type) {
            this.type = type;
        }
    }
}
