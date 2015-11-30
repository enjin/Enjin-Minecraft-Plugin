package com.enjin.bungee.util.io;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.LogRecord;

public class EnjinLogFormatter extends Formatter {
    private DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss z");

    public String format(LogRecord rec) {
        StringBuffer buf = new StringBuffer(5);
        buf.append(calcDate(rec.getMillis()) + " ");
        buf.append("[" + rec.getLevel() + "]");
        buf.append(' ');
        buf.append(formatMessage(rec) + "\n");
        return buf.toString();
    }

    private String calcDate(long millisecs) {
        Date resultdate = new Date(millisecs);
        return dateFormat.format(resultdate);
    }

    public String getHead(Handler h) {
        return "Started logging the Enjin Plugin on "
                + calcDate(System.currentTimeMillis()) + "\n";
    }

    public String getTail(Handler h) {
        return "";
    }
}