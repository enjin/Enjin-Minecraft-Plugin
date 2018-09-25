package com.enjin.bungee.util;

import com.enjin.bungee.EnjinMinecraftPlugin;
import com.enjin.bungee.util.io.EnjinLogFormatter;
import com.enjin.core.Enjin;
import com.enjin.core.util.EnjinLogger;
import lombok.Getter;
import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.model.ZipParameters;
import net.lingala.zip4j.util.Zip4jConstants;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Log implements EnjinLogger {

    private static final SimpleDateFormat LOG_ZIP_NAME_FORMAT = new SimpleDateFormat("yyyy-MM-dd");

    @Getter
    private final static Logger logger = Logger.getLogger(EnjinMinecraftPlugin.class.getName());
    private              File   logs   = null;
    private              File   log    = null;

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
        logger.warning(hideSensitiveText(msg));
    }

    public void debug(String msg) {
        if (Enjin.getConfiguration() != null && Enjin.getConfiguration().isDebug()) {
            logger.info("[Debug] " + hideSensitiveText(msg));
        }
    }

    @Override
    public void log(String msg, Throwable t) {
        logger.log(Level.SEVERE, msg, t);
    }

    @Override
    public void log(Throwable t) {
        log("An error occurred...", t);
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
        if (Enjin.getConfiguration() == null || Enjin.getConfiguration()
                                                     .getAuthKey() == null || Enjin.getConfiguration()
                                                                                   .getAuthKey()
                                                                                   .isEmpty()) {
            return msg;
        } else {
            return msg.replaceAll(Enjin.getConfiguration().getAuthKey(),
                                  "**************************************************");
        }
    }

    public void configure() {
        if (Enjin.getConfiguration().isLoggingEnabled()) {
            EnjinLogFormatter formatter = new EnjinLogFormatter();
            FileHandler       handler   = null;

            try {
                handler = new FileHandler(EnjinMinecraftPlugin.getInstance()
                                                              .getDataFolder()
                                                              .getAbsolutePath() + File.separator + "logs" + File.separator + "enjin.log",
                                          true);
            } catch (IOException e) {
                Enjin.getLogger().log(e);
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
