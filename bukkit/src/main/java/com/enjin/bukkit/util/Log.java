package com.enjin.bukkit.util;

import com.enjin.bukkit.EnjinMinecraftPlugin;
import com.enjin.bukkit.util.io.LineAppender;
import com.enjin.core.Enjin;
import com.enjin.core.util.EnjinLogger;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.Logger;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.appender.ConsoleAppender;
import org.apache.logging.log4j.core.appender.FileAppender;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.helpers.Charsets;
import org.apache.logging.log4j.core.layout.PatternLayout;

import java.io.File;
import java.io.IOException;

public class Log implements EnjinLogger {
    private Logger logger = (Logger) LogManager.getLogger(EnjinMinecraftPlugin.class.getSimpleName());
    private LineAppender listener = null;

    public Log(File configDir) {
        logger.setLevel(org.apache.logging.log4j.Level.DEBUG);

        File logs = new File(configDir, "logs");
        File log = new File(logs, "enjin.log");

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

        configure(logger, log);
        debug("Log Utility Initialized");
    }

    public void info(String msg) {
        logger.info("[EMP INFO] " + msg);
    }

    public void fine(String msg) {
        logger.info("[EMP FINE] " + msg);
    }

    public void warning(String msg) {
        logger.warn("[EMP WARN] " + msg);
    }

    public void debug(String msg) {
        logger.info("[EMP DEBUG] " + msg);
    }

    public String getLastLine() {
        return listener.getLine();
    }

    private void configure(Logger logger, File log) {
        LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
        Configuration config = ctx.getConfiguration();
        PatternLayout layout = PatternLayout.createLayout("[%d{HH:mm:ss}] [%t/%level] [%logger]: %msg%n", config, null, Charsets.UTF_8.name(), null);

        listener = new LineAppender("EnjinLineIn", layout);
        listener.start();
        Logger root = (Logger) LogManager.getRootLogger();
        root.addAppender(listener);

        if (Enjin.getConfiguration().isLoggingEnabled()) {
            layout = PatternLayout.createLayout("[%d{yyyy-MM-dd HH:mm:ss}]: %msg%n", config, null, Charsets.UTF_8.name(), null);
            FileAppender fileAppender = FileAppender.createAppender(log.getPath(), null, "true", "EnjinFileOut", "false", null, null, layout, null, null, null, config);
            fileAppender.start();
            logger.addAppender(fileAppender);
        }
    }
}
