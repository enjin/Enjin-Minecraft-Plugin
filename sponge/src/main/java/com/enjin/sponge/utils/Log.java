package com.enjin.sponge.utils;

import com.enjin.core.Enjin;
import com.enjin.core.util.EnjinLogger;
import com.enjin.sponge.EnjinMinecraftPlugin;
import com.enjin.sponge.utils.io.LineAppender;
import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.model.ZipParameters;
import net.lingala.zip4j.util.Zip4jConstants;
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
import java.io.FileInputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class Log implements EnjinLogger {

    private static final SimpleDateFormat LOG_ZIP_NAME_FORMAT = new SimpleDateFormat("yyyy-MM-dd");

    private Logger logger = (Logger) LogManager.getLogger(EnjinMinecraftPlugin.class.getSimpleName());
    private LineAppender listener;
    private File logs = null;
    private File log = null;

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
            parameters.setCompressionLevel(Zip4jConstants.DEFLATE_LEVEL_MAXIMUM);
            zip.addStream((fis = new FileInputStream(log)), parameters);
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

        try {
            log.delete();
            log.createNewFile();
        } catch (Exception e) {
            Enjin.getLogger().log(e);
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
            logger.info("[Debug] " + hideSensitiveText(msg));
        }
    }

    @Override
    public void log(String msg, Throwable t) {
        logger.error(msg);
        logger.catching(t);
    }

    @Override
    public void log(Throwable t) {
        logger.catching(t);
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
        LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
        Configuration config = ctx.getConfiguration();
        PatternLayout layout = PatternLayout.createLayout("[%d{yyyy-MM-dd HH:mm:ss}]: %msg%n", config, null, Charsets.UTF_8.name(), null);

        if (Enjin.getConfiguration().isLoggingEnabled()) {
            FileAppender fileAppender = FileAppender.createAppender(log.getPath(), null, "true", "EnjinFileOut", null, null, null, layout, null, null, null, config);
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
