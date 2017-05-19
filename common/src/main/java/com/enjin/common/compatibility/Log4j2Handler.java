package com.enjin.common.compatibility;

import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.appender.FileAppender;
import org.apache.logging.log4j.core.layout.PatternLayout;

public interface Log4j2Handler {

    PatternLayout createPatternLayout(LoggerContext ctx) throws Throwable;

    FileAppender createFileAppender(LoggerContext ctx, String name, String path) throws Throwable;

}
