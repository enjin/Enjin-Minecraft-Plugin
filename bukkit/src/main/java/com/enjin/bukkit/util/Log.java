package com.enjin.bukkit.util;

import com.enjin.bukkit.EnjinMinecraftPlugin;
import com.enjin.bukkit.util.io.EnjinLogAppender;
import com.enjin.bukkit.util.io.EnjinLogFormatter;
import com.enjin.bukkit.util.io.EnjinLogInterface;
import com.enjin.core.util.EnjinLogger;
import lombok.Getter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Appender;

import java.io.File;
import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Log implements EnjinLogger {
    @Getter
    private Logger logger = Logger.getLogger(EnjinMinecraftPlugin.class.getName());
    private EnjinLogInterface mcLogListener = new EnjinLogAppender();

    public Log() {
        debug("Initializing internal logger");
        logger.setLevel(Level.FINEST);

        File logs = new File(EnjinMinecraftPlugin.getInstance().getDataFolder(), "logs");
        File log = new File(logs, "enjin.log");
        if (log.exists()) {
            //Max file size of the enjin log should be less than 5MB.
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

        org.apache.logging.log4j.core.Logger log4j = (org.apache.logging.log4j.core.Logger) LogManager.getRootLogger();
        log4j.addAppender((Appender) mcLogListener);
    }

    public void info(String msg) {
        logger.info(msg);
    }

    public void fine(String msg) {
        logger.fine(msg);
    }

    public void warning(String msg) {
        logger.warning(msg);
    }

    public void debug(String msg) {
        fine("Enjin Debug: " + msg);
    }

    public String getLastLine() {
        return mcLogListener.getLine();
    }
}
