package com.enjin.core.util;

import java.io.File;

public interface EnjinLogger {
    void info(String msg);

    void warning(String msg);

    void debug(String msg);

    String getLastLine();

    void setDebug(boolean debug);

    File getLogDirectory();

    File getLogFile();
}
