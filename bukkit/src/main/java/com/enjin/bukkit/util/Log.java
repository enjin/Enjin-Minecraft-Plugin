package com.enjin.bukkit.util;

import com.enjin.bukkit.EnjinMinecraftPlugin;
import com.enjin.bukkit.util.io.LineAppender;
import com.enjin.core.Enjin;
import com.enjin.core.util.EnjinLogger;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.*;
import org.apache.logging.log4j.core.Logger;
import org.apache.logging.log4j.core.appender.FileAppender;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.filter.AbstractFilterable;
import org.apache.logging.log4j.core.filter.ThresholdFilter;
import org.apache.logging.log4j.core.helpers.Charsets;
import org.apache.logging.log4j.core.layout.PatternLayout;

import java.io.File;
import java.io.IOException;
import java.util.Map;

public class Log implements EnjinLogger {
    private Logger logger = (Logger) LogManager.getLogger(EnjinMinecraftPlugin.class.getName());
	private Level defaultLevel = logger.getLevel();
    private LineAppender lineAppender = null;
	private FileAppender logAppender = null;
	private File logs = null;
	private File log = null;
	private boolean configured = false;
	private boolean debug = false;

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
		if (this.debug) {
			logger.debug(hideSensitiveText(msg));
		}
	}

	public void catching(Throwable e) {
		logger.catching(e);
	}

	public String getLastLine() {
		return lineAppender.getLine();
	}

	private String hideSensitiveText(String msg) {
		if (msg == null || msg.isEmpty() || Enjin.getConfiguration() == null || Enjin.getConfiguration().getAuthKey() == null
				|| Enjin.getConfiguration().getAuthKey().isEmpty()) {
			return msg;
		} else {
			return msg.replaceAll(Enjin.getConfiguration().getAuthKey(), "**************************************************");
		}
	}

    public void configure() {
		if (configured) {
			return;
		}

		configured = true;

		LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
        Configuration config = ctx.getConfiguration();
        PatternLayout layout = PatternLayout.createLayout("[%d{yyyy-MM-dd HH:mm:ss} %-5p]: %msg%n", config, null, Charsets.UTF_8.name(), null);

		if (Enjin.getConfiguration().isLoggingEnabled()) {
			FileAppender fileAppender = FileAppender.createAppender(log.getPath(), null, "true", "EnjinLog", "false", null, null, layout, null, null, null, config);
			fileAppender.start();
			logger.addAppender(fileAppender);
		}

		lineAppender = new LineAppender("EnjinLineIn", layout);
		lineAppender.start();
		Logger root = (Logger) LogManager.getRootLogger();
		root.addAppender(lineAppender);

		setDebug(false);
    }

	public void setDebug(boolean debug) {
		this.debug = debug;

		if (this.debug) {
			logger.setLevel(Level.DEBUG);
		} else {
			logger.setLevel(defaultLevel);
		}

		for (Map.Entry<String, Appender> entry : this.logger.getAppenders().entrySet()) {
			if (entry.getValue() instanceof AbstractFilterable && !(entry.getValue() instanceof FileAppender)) {
				AbstractFilterable appender = (AbstractFilterable) entry.getValue();
				if (debug) {
					appender.stopFilter();
					appender.addFilter(ThresholdFilter.createFilter(Level.ALL.name(), "ACCEPT", "DENY"));
					appender.startFilter();
				} else {
					appender.stopFilter();
					appender.addFilter(ThresholdFilter.createFilter(Level.INFO.name(), "ACCEPT", "DENY"));
					appender.startFilter();
				}
			}
		}
	}
}
