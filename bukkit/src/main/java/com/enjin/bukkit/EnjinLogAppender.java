package com.enjin.bukkit;

import java.io.Serializable;

import lombok.Getter;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.ErrorHandler;
import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.LogEvent;

public class EnjinLogAppender implements Appender, EnjinLogInterface {
    @Getter
    String line = "";

    @Override
    public boolean isStarted() {
        return true;
    }

    @Override
    public void start() {}

    @Override
    public void stop() {}

    @Override
    public void append(LogEvent event) {
        if (event != null && event.getMessage() != null && event.getMessage().getFormattedMessage() != null) {
            line = event.getMessage().getFormattedMessage();
            line = line.replaceAll("\\p{Cntrl}.{2}", "");
            line = line.replaceAll("\\p{Cntrl}", "");
        }
    }

    @Override
    public ErrorHandler getHandler() {
        return null;
    }

    @Override
    public Layout<? extends Serializable> getLayout() {
        return null;
    }

    @Override
    public String getName() {
        return "EnjinLogListener";
    }

    @Override
    public boolean ignoreExceptions() {
        return false;
    }

    @Override
    public void setHandler(ErrorHandler arg0) {}
}
