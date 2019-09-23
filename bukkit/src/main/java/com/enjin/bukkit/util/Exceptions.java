package com.enjin.bukkit.util;

import java.io.PrintWriter;
import java.io.StringWriter;

public class Exceptions {

    public static String getStackTrace(Throwable t) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        t.printStackTrace(pw);
        return sw.toString();
    }

}
