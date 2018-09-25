package com.enjin.bukkit.util;

import com.enjin.bukkit.EnjinMinecraftPlugin;
import com.enjin.bukkit.util.io.LineAppender;
import com.enjin.common.Log4j2Handlers;
import com.enjin.common.compatibility.Log4j2Handler;
import com.enjin.core.Enjin;
import com.enjin.core.util.EnjinLogger;
import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.model.ZipParameters;
import net.lingala.zip4j.util.Zip4jConstants;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.Logger;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.appender.FileAppender;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class Log implements EnjinLogger {

    private static final SimpleDateFormat LOG_ZIP_NAME_FORMAT = new SimpleDateFormat("yyyy-MM-dd");

    private Logger       logger       = (Logger) LogManager.getLogger(EnjinMinecraftPlugin.class.getName());
    private LineAppender lineAppender = null;
    private FileAppender logAppender  = null;
    private File         logs         = null;
    private File         log          = null;
    private boolean      configured   = false;

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
            int    i    = 0;
            File   file = null;
            while (file == null || file.exists()) {
                file = new File(logs, date + "-" + ++i + ".log.zip");
            }

            ZipFile       zip        = new ZipFile(file);
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
        logger.info("[DEBUG] " + hideSensitiveText(msg));
    }

    @Override
    public void log(String msg, Throwable t) {
        logger.log(Level.ERROR, msg, t);
    }

    @Override
    public void log(Throwable t) {
        log("Exception Caught: ", t);
    }

    public String getLastLine() {
        return lineAppender.getLine();
    }

    private String hideSensitiveText(String msg) {
        if (msg == null || msg.isEmpty() || Enjin.getConfiguration() == null || Enjin.getConfiguration()
                                                                                     .getAuthKey() == null
                || Enjin.getConfiguration().getAuthKey().isEmpty()) {
            return msg;
        } else {
            return msg.replaceAll(Enjin.getConfiguration().getAuthKey(),
                                  "**************************************************");
        }
    }

    public void configure() {
        if (configured) {
            return;
        }

        Log4j2Handler log4j2Handler = Log4j2Handlers.findHandler();
        LoggerContext ctx           = (LoggerContext) LogManager.getContext(false);

        if (log4j2Handler != null) {
            for (Appender appender : logger.getAppenders().values()) {
                ((AbstractAppender) appender).addFilter(LogFilter.DEFAULT);
            }

            if (Enjin.getConfiguration().isLoggingEnabled()) {
                try {
                    logAppender = log4j2Handler.createFileAppender(ctx, "EnjinFileOut", log.getPath());
                    logAppender.addFilter(LogFilter.FILE_APPENDER_FILTER);
                    logAppender.start();
                    logger.addAppender(logAppender);
                } catch (Throwable t) {
                    warning("Could not initialize file appender...");
                }
            }

            try {
                lineAppender = new LineAppender("EnjinLineIn", log4j2Handler.createPatternLayout(ctx));
                lineAppender.addFilter(LogFilter.DEFAULT);
                lineAppender.start();
                Logger root = (Logger) LogManager.getRootLogger();
                root.addAppender(lineAppender);
            } catch (Throwable t) {
                warning("Could not initialize line appender...");
            }
        }

        setDebug(false);
        configured = true;
    }

    public void setDebug(boolean debug) {
        LogFilter.DEFAULT.setDebug(debug);
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
