package com.enjin.sponge.utils.io;

import lombok.Getter;
import org.apache.logging.log4j.core.*;
import org.apache.logging.log4j.core.appender.AbstractAppender;

import java.io.Serializable;

public class LineAppender extends AbstractAppender {
    @Getter
    String line = "";

    public LineAppender(String name, Layout<? extends Serializable> layout) {
        super(name, null, layout);
    }

    @Override
    public void append(LogEvent event) {
        if (event != null && event.getMessage() != null && event.getMessage().getFormattedMessage() != null) {
            line = event.getMessage().getFormattedMessage();
            line = line.replaceAll("\\p{Cntrl}.{2}", "");
            line = line.replaceAll("\\p{Cntrl}", "");
        }
    }
}
