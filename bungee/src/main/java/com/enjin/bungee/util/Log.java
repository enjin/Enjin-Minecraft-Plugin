package com.enjin.bungee.util;

import com.enjin.bungee.EnjinMinecraftPlugin;
import com.enjin.bungee.util.io.EnjinLogFormatter;
import lombok.Getter;

import java.io.File;
import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Log {
    @Getter
    private final static Logger logger = Logger.getLogger(EnjinMinecraftPlugin.class.getName());

    public static void info(String msg) {
        logger.info(msg);
    }

    public static void fine(String msg) {
        logger.fine(msg);
    }

    public static void warning(String msg) {
        logger.warning(msg);
    }

    public static void debug(String msg) {
        fine("Enjin Debug: " + msg);
    }

    public static void init() {
        debug("Initializing internal logger");
        logger.setLevel(Level.FINEST);

        File logs = new File(EnjinMinecraftPlugin.getInstance().getDataFolder(), "logs");
        File log = new File(logs, "enjin.log");
        if (log.exists()) {
            if (log.length() > 1024 * 1024 * 5) {
                log.delete();
            }
        } else {
            logs.mkdirs();

            try {
                log.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        EnjinLogFormatter formatter = new EnjinLogFormatter();
        FileHandler handler = null;
        try {
            handler = new FileHandler(EnjinMinecraftPlugin.getInstance().getDataFolder().getAbsolutePath() + File.separator + "logs" + File.separator + "enjin.log", true);
        } catch (IOException e) {
            e.printStackTrace();
        }

        handler.setFormatter(formatter);
        logger.addHandler(handler);
        logger.setUseParentHandlers(false);
        debug("Logger initialized.");
    }
}
