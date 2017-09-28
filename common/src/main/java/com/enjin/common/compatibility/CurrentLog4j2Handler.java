package com.enjin.common.compatibility;

import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.appender.FileAppender;
import org.apache.logging.log4j.core.layout.PatternLayout;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.nio.charset.Charset;

public class CurrentLog4j2Handler implements Log4j2Handler {

    private static MethodHandle patternLayoutNewBuilder;
    private static MethodHandle fileAppenderNewBuilder;
    public static boolean detected;

    static {
        try {
            patternLayoutNewBuilder = MethodHandles.lookup().findStatic(PatternLayout.class, "newBuilder",
                    MethodType.methodType(PatternLayout.Builder.class));
            fileAppenderNewBuilder = MethodHandles.lookup().findStatic(FileAppender.class, "newBuilder",
                    MethodType.methodType(FileAppender.Builder.class));
            detected = true;
        } catch (Exception e) {
            detected = false;
        }
    }

    @Override
    public PatternLayout createPatternLayout(LoggerContext ctx) throws Throwable {
        return PatternLayout.newBuilder()
                .withPattern("[%d{yyyy-MM-dd HH:mm:ss} %p] %msg%n")
                .withConfiguration(ctx.getConfiguration())
                .withCharset(Charset.forName("UTF-8"))
                .build();
    }

    @Override
    public FileAppender createFileAppender(LoggerContext ctx, String name, String path) throws Throwable {
        FileAppender.Builder builder = (FileAppender.Builder) FileAppender.newBuilder()
                .withFileName(path)
                .withName(name)
                .withLayout(createPatternLayout(ctx));
        return builder.build();
    }

}
