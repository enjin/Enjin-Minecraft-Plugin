package com.enjin.bukkit.util;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.filter.AbstractFilter;

@NoArgsConstructor @AllArgsConstructor
public class LogFilter extends AbstractFilter {

    public static final MutableLogFilter DEFAULT = new MutableLogFilter();
    public static final LogFilter FILE_APPENDER_FILTER = new LogFilter(true);

    @Getter
    protected boolean debug = false;

    @Override
    public Result filter(LogEvent event) {
        String message = event.getMessage().getFormattedMessage().trim().toLowerCase();
        if (!debug && message.startsWith("[debug]")) {
            return Result.DENY;
        } else {
            return Result.ACCEPT;
        }
    }

    public static class MutableLogFilter extends LogFilter {

        public void setDebug(boolean debug) {
            this.debug = debug;
        }

    }

}
