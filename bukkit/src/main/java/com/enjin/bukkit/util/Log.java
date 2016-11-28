package com.enjin.bukkit.util;

import com.enjin.bukkit.EnjinMinecraftPlugin;
import com.enjin.bukkit.util.io.LineAppender;
import com.enjin.core.Enjin;
import com.enjin.core.util.EnjinLogger;
import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.model.ZipParameters;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.MarkerManager;
import org.apache.logging.log4j.ThreadContext;
import org.apache.logging.log4j.core.*;
import org.apache.logging.log4j.core.Logger;
import org.apache.logging.log4j.core.appender.FileAppender;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.helpers.Charsets;
import org.apache.logging.log4j.core.impl.Log4jLogEvent;
import org.apache.logging.log4j.core.layout.PatternLayout;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class Log implements EnjinLogger {
    private static final SimpleDateFormat LOG_ZIP_NAME_FORMAT = new SimpleDateFormat("yyyy-MM-dd");

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
                zipAndReplaceExistingLog();
            } else {
                logs.mkdirs();
                log.createNewFile();
            }
        } catch (IOException e) {
            Enjin.getLogger().log(e);
        }
    }

    private void zipAndReplaceExistingLog() {
        FileInputStream fis = null;
        try {
            String date = LOG_ZIP_NAME_FORMAT.format(Calendar.getInstance().getTime());
            int i = 0;
            File file = null;
            while (file == null || file.exists()) {
                file = new File(logs, date + "-" + ++i + ".log.zip");
            }

            ZipFile zip = new ZipFile(file);
            ZipParameters parameters = new ZipParameters();
            parameters.setFileNameInZip(date + "-" + i + ".log");
            parameters.setSourceExternalStream(true);
            zip.addStream((fis = new FileInputStream(log)), parameters);

            log.delete();
            log.createNewFile();
        } catch (Exception e) {
            Enjin.getLogger().log(e);
        } finally {
            try {
                if (fis != null) {
                    fis.close();
                }
            } catch (Exception e) {
                Enjin.getLogger().log(e);
            }
        }
    }

    public void info(String msg) {
        logger.info(hideSensitiveText(msg));
    }

    public void warning(String msg) {
        logger.warn(hideSensitiveText(msg));
    }

    public void debug(String msg) {
        if (Enjin.getConfiguration().isDebug()) {
            logger.info("[DEBUG] " + hideSensitiveText(msg));
        } else if (Enjin.getConfiguration().isLoggingEnabled() && logAppender != null) {
            logAppender.append(Log4jLogEvent.createEvent(EnjinMinecraftPlugin.class.getName(),
                    MarkerManager.getMarker("debug"),
                    EnjinMinecraftPlugin.class.getName(),
                    Level.DEBUG,
                    logger.getMessageFactory().newMessage(hideSensitiveText(msg)),
                    null,
                    ThreadContext.getImmutableContext(),
                    ThreadContext.getImmutableStack(),
                    Thread.currentThread().getName(),
                    null,
                    System.currentTimeMillis()));
        }
    }

    @Override
    public void log(String msg, Throwable t) {
        logger.log(Level.ERROR, msg, t);
    }

    @Override
    public void log(Throwable t) {
        logger.log(Level.ERROR, t);
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
        PatternLayout layout = PatternLayout.createLayout("[%d{yyyy-MM-dd HH:mm:ss} %p]: %msg%n", config, null, Charsets.UTF_8.name(), null);

        if (Enjin.getConfiguration().isLoggingEnabled()) {
            logAppender = FileAppender.createAppender(log.getPath(), null, "true", "EnjinLog", "false", null, "false", layout, null, null, null, config);
            logAppender.start();
            logger.addAppender(logAppender);
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
