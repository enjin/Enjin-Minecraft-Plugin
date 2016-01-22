package com.enjin.core.util;

public interface EnjinLogger {
    void info(String msg);
    void warning(String msg);
    void fine(String msg);
    void debug(String msg);
    String getLastLine();
}
