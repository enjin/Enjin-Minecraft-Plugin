package com.enjin.bungee.util;

import com.enjin.bungee.EnjinMinecraftPlugin;
import com.enjin.bungee.util.io.EnjinLogFormatter;
import com.enjin.core.Enjin;
import com.enjin.core.util.EnjinLogger;
import lombok.Getter;

import java.io.File;
import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Log implements EnjinLogger {
    @Getter
    private final static Logger logger = Logger.getLogger(EnjinMinecraftPlugin.class.getName());
    private File logs = null;
    private File log = null;

    public Log(File configDir) {
        logs = new File(configDir, "logs");
        log = new File(logs, "enjin.log");

        try {
            if (log.exists()) {
                //Max file size of the enjin log should be less than 5MB.
                if (log.length() > 1024 * 1024 * 5) {
                    log.delete();
                    log.createNewFile();
                }
            } else {
                logs.mkdirs();
                log.createNewFile();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void info(String msg) {
        logger.info(hideSensitiveText(msg));
    }

    public void warning(String msg) {
        logger.warning(hideSensitiveText(msg));
    }

    public void debug(String msg) {
        if (Enjin.getConfiguration() != null && Enjin.getConfiguration().isDebug()) {
            logger.info("[Debug] " + hideSensitiveText(msg));
        }
    }

    @Override
    public String getLastLine() {
        return "";
    }

    @Override
    public void setDebug(boolean debug) {
        // TODO
    }

    private String hideSensitiveText(String msg) {
        if (Enjin.getConfiguration() == null || Enjin.getConfiguration().getAuthKey() == null || Enjin.getConfiguration().getAuthKey().isEmpty()) {
            return msg;
        } else {
            return msg.replaceAll(Enjin.getConfiguration().getAuthKey(),
                    "**************************************************");
        }
    }

    public void configure() {
        if (Enjin.getConfiguration().isLoggingEnabled()) {
            EnjinLogFormatter formatter = new EnjinLogFormatter();
            FileHandler handler = null;

            try {
                handler = new FileHandler(EnjinMinecraftPlugin.getInstance().getDataFolder().getAbsolutePath() + File.separator + "logs" + File.separator + "enjin.log", true);
            } catch (IOException e) {
                e.printStackTrace();
            }

            handler.setFormatter(formatter);
            logger.addHandler(handler);
        }

        logger.setUseParentHandlers(false);
        logger.setLevel(Level.FINEST);
    }

    @Override
    public File getLogDirectory() {
        return logs;
    }

    @Override
    public File getLogFile() {
        return log;
    }
}
