package com.enjin.sponge.utils;

import com.enjin.core.Enjin;
import com.enjin.core.util.EnjinLogger;
import com.enjin.sponge.EnjinMinecraftPlugin;
import com.enjin.sponge.utils.io.LineAppender;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.Logger;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.appender.ConsoleAppender;
import org.apache.logging.log4j.core.appender.FileAppender;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.filter.ThresholdFilter;
import org.apache.logging.log4j.core.helpers.Charsets;
import org.apache.logging.log4j.core.layout.PatternLayout;

import java.io.File;
import java.io.IOException;

public class Log implements EnjinLogger {
    private Logger logger = (Logger) LogManager.getLogger(EnjinMinecraftPlugin.class.getSimpleName());
    private LineAppender listener;
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
			Enjin.getLogger().catching(e);
        }
    }

    public void info(String msg) {
        logger.info(hideSensitiveText(msg));
    }

    public void fine(String msg) {
        debug(hideSensitiveText(msg));
    }

    public void warning(String msg) {
        logger.warn(hideSensitiveText(msg));
    }

    public void debug(String msg) {
        if (Enjin.getConfiguration().isDebug()) {
            logger.info("[Debug] " + hideSensitiveText(msg));
        }
    }

	public void catching(Throwable e) {
		logger.catching(e);
	}

    public String getLastLine() {
        return listener.getLine();
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
//		logger.getAppenders().forEach((string, appender) -> {
//			logger.removeAppender(appender);
//		});

        LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
        Configuration config = ctx.getConfiguration();
        PatternLayout layout = PatternLayout.createLayout("[%d{yyyy-MM-dd HH:mm:ss}]: %msg%n", config, null, Charsets.UTF_8.name(), null);

        if (Enjin.getConfiguration().isLoggingEnabled()) {
            FileAppender fileAppender = FileAppender.createAppender(log.getPath(), null, "true", "EnjinFileOut", "false", null, null, layout, null, null, null, config);
            fileAppender.start();
            logger.addAppender(fileAppender);
        }

		listener = new LineAppender("EnjinLineIn", layout);
		listener.start();
		Logger root = (Logger) LogManager.getRootLogger();
		root.addAppender(listener);

        logger.setLevel(Level.DEBUG);
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
