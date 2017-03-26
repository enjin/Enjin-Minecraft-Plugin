package com.enjin.core.util;

import java.io.PrintWriter;
import java.io.StringWriter;

public final class StringUtils {

    public static String throwableToString(Throwable throwable) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        throwable.printStackTrace(pw);
        String stack = pw.toString();
        pw.close();
        return stack;
    }

}
