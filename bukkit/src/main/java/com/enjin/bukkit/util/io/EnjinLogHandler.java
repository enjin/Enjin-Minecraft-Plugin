package com.enjin.bukkit.util.io;

import lombok.Getter;

import java.util.logging.Handler;
import java.util.logging.LogRecord;

public class EnjinLogHandler extends Handler implements EnjinLogInterface {
    @Getter
    private String line = "";

    @Override
    public void close() throws SecurityException {}

    @Override
    public void flush() {}

    @Override
    public void publish(LogRecord record) {
        if (record.getMessage() == null) {
            return;
        }

        line = record.getMessage();
        line = line.replaceAll("\\p{Cntrl}.{2}", "");
        line = line.replaceAll("\\p{Cntrl}", "");
    }
}
