package com.enjin.common.compatibility;

import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.appender.FileAppender;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.layout.PatternLayout;
import org.apache.logging.log4j.core.pattern.RegexReplacement;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.nio.charset.Charset;

public class LegacyLog4j2Handler implements Log4j2Handler {

    private static MethodHandle patternLayoutCreateLayout;
    private static MethodHandle fileAppenderCreateAppender;
    public static  boolean      detected;

    static {
        try {
            patternLayoutCreateLayout = MethodHandles.lookup().findStatic(PatternLayout.class, "createLayout",
                                                                          MethodType.methodType(PatternLayout.class,
                                                                                                String.class,
                                                                                                Configuration.class,
                                                                                                RegexReplacement.class,
                                                                                                String.class,
                                                                                                String.class));
            fileAppenderCreateAppender = MethodHandles.lookup().findStatic(FileAppender.class, "createAppender",
                                                                           MethodType.methodType(FileAppender.class,
                                                                                                 String.class,
                                                                                                 String.class,
                                                                                                 String.class,
                                                                                                 String.class,
                                                                                                 String.class,
                                                                                                 String.class,
                                                                                                 String.class,
                                                                                                 Layout.class,
                                                                                                 Filter.class,
                                                                                                 String.class,
                                                                                                 String.class,
                                                                                                 Configuration.class));
            detected = true;
        } catch (Exception e) {
            detected = false;
        }
    }

    @Override
    public PatternLayout createPatternLayout(LoggerContext ctx) throws Throwable {
        Configuration configuration = ctx.getConfiguration();
        PatternLayout layout = (PatternLayout) patternLayoutCreateLayout.invoke(
                "[%d[yyyy-MM-dd HH:mm:ss} %p] %msg%n",
                configuration,
                null,
                Charset.forName("UTF-8").name(),
                null
        );
        return layout;
    }

    @Override
    public FileAppender createFileAppender(LoggerContext ctx, String name, String path) throws Throwable {
        Configuration configuration = ctx.getConfiguration();
        FileAppender appender = (FileAppender) fileAppenderCreateAppender.invoke(
                path, // fileName
                null, // append
                null, // locking
                name, // name
                null, // immediateFlush
                null, // ignore
                null, // bufferedIO
                createPatternLayout(ctx), // layout
                null, // filter
                null, // advertise
                null, // advertiseUri
                configuration // config
        );
        return appender;
    }

}
