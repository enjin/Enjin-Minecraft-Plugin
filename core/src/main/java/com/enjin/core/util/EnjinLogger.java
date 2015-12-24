package com.enjin.core.util;

public interface EnjinLogger {
    public void info(String msg);
    public void warning(String msg);
    public void fine(String msg);
    public void debug(String msg);
    public String getLastLine();
}
