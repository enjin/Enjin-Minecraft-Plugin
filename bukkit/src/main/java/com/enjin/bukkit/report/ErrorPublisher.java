package com.enjin.bukkit.report;

import com.enjin.bukkit.util.io.FileUtil;
import com.enjin.bukkit.util.text.TextBuilder;
import com.enjin.core.Enjin;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class ErrorPublisher extends BukkitRunnable {
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss a z")
            .withZone(ZoneOffset.UTC);
    private static final DateTimeFormatter FILE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH.mm.ss")
            .withZone(ZoneOffset.UTC);
    private static final int BORDER_WIDTH = 80;
    private static final String HEADER_TITLE = "Error Log";
    private static final String SUMMARY_TITLE = "Summary";
    private static final String PLUGINS_TITLE = "Offending Plugins";
    private static final String PLUGINS_SUBTITLE = "Report issue to author of first plugin";
    private static final String ERROR_TITLE = "Stack Trace";

    private final Plugin plugin;
    private final Throwable throwable;
    private final Thread thread;
    private final TextBuilder report;
    private final Instant time;
    private int framesInCommon;

    public ErrorPublisher(Plugin plugin, Throwable throwable) {
        this.plugin = plugin;
        this.throwable = throwable;
        this.thread = Thread.currentThread();
        this.report = new TextBuilder();
        this.time = Instant.now();
        this.report.setBorderWidth(BORDER_WIDTH);
    }

    @Override
    public void run() {
        addHeader();
        addSummary();
        addOffendingPlugins();
        addError();
        save();
    }

    private void addHeader() {
        report.header(HEADER_TITLE);
        report.append("Time: ").append(TIME_FORMAT.format(Instant.now())).newLine();
        report.append("Thread: ").append(thread.getName()).newLine();
        report.append("Plugin Version: ").append(plugin.getDescription().getVersion()).newLine();
    }

    private void addSummary() {
        Throwable cause = getOriginalCause(throwable);
        StackTraceElement element = cause.getStackTrace()[0];
        report.header(SUMMARY_TITLE);
        report.append("Cause: ").append(cause.getClass().getName()).newLine();
        report.append("Message: ").append(cause.getMessage()).newLine();
        report.append("Method: ").append(element.getClassName()).append('.')
                .append(element.getMethodName()).newLine();
        report.append("File: ").append(element.getFileName()).newLine();
        report.append("Line: ").append(element.getLineNumber()).newLine();
    }

    private void addOffendingPlugins() {
        List<Plugin> plugins = getPluginsInStack();
        report.header(PLUGINS_TITLE, PLUGINS_SUBTITLE);
        if (plugins.isEmpty()) {
            report.append("no plugins detected in stack trace").newLine();
        } else {
            for (Plugin plugin : plugins)
                report.append(plugin.getName()).append(" ").append(plugin.getDescription().getVersion()).newLine();
        }
    }

    private void addError() {
        Throwable t = throwable;
        report.header(ERROR_TITLE);
        while (t != null) {
            report.append("Caused by: ").append(t.getClass().getName()).newLine();
            printStack(t);
            t = t.getCause();
        }
    }

    private List<Plugin> getPluginsInStack() {
        Throwable t = getOriginalCause(throwable);
        StackTraceElement[] stack = t.getStackTrace();
        List<Plugin> out = new ArrayList<>();
        for (StackTraceElement element : stack) {
            String clazzName = element.getClassName();
            Class clazz;

            try {
                clazz = Class.forName(clazzName);
            } catch (ClassNotFoundException e) {
                continue;
            }

            Plugin plugin = getOwningPlugin(clazz);
            if (plugin != null && !out.contains(plugin))
                out.add(plugin);
        }
        return out;
    }

    private Plugin getOwningPlugin(Class clazz) {
        ClassLoader cl = clazz.getClassLoader();
        for (Plugin plugin : Bukkit.getPluginManager().getPlugins()) {
            if (plugin.getClass().getClassLoader() == cl)
                return plugin;
        }
        return null;
    }

    private Throwable getOriginalCause(Throwable t) {
        if (t.getCause() == null)
            return t;
        return t.getCause();
    }

    private void printStack(Throwable t) {
        StackTraceElement[] tStack = t.getStackTrace();
        int endIndex = getDivergentIndex(t, t.getCause());
        for (int i = 0; i <= endIndex; i++) {
            StackTraceElement element = tStack[i];
            report.indent(1, true).append("at ")
                    .append(element.getClassName()).append('.')
                    .append(element.getMethodName()).append('(')
                    .append(element.getFileName()).append(':')
                    .append(element.getLineNumber()).append(')').newLine();
        }
        if (framesInCommon > 0)
            report.indent(1, true).append("... ")
                    .append(framesInCommon).append(" more").newLine();
        resetFrames();
    }

    private int getDivergentIndex(Throwable t, Throwable c) {
        StackTraceElement[] tStack = t.getStackTrace();

        if (c == null)
            return tStack.length - 1;

        StackTraceElement[] cStack = c.getStackTrace();
        int tIndex = tStack.length - 1;
        int cIndex = cStack.length - 1;

        while (tIndex >= 0 && cIndex >= 0 && tStack[tIndex].equals(cStack[cIndex])) {
            tIndex--;
            cIndex--;
        }

        framesInCommon = cStack.length - 1 - cIndex;

        return tIndex;
    }

    private void resetFrames() {
        framesInCommon = 0;
    }

    @Override
    public String toString() {
        return report.toString();
    }

    private void save() {
        File folder = new File(plugin.getDataFolder(), "errors/");
        String fileName = getOriginalCause(throwable).getClass().getSimpleName() + "-"
                + FILE_FORMAT.format(time) + ".txt";
        File file = new File(folder, fileName);
        try {
            FileUtil.write(file, toString());
        } catch (Throwable t) {
            Enjin.getLogger().log(t);
        }
    }
}
