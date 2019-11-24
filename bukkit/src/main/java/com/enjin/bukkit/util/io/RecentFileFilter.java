package com.enjin.bukkit.util.io;

import java.io.File;
import java.io.FileFilter;
import java.time.Clock;
import java.time.Instant;
import java.util.concurrent.TimeUnit;

public class RecentFileFilter implements FileFilter {

    private long threshold;

    public RecentFileFilter(long seconds) {
        this.threshold = TimeUnit.SECONDS.toMillis(seconds);
    }

    @Override
    public boolean accept(File file) {
        long modificationTime = file.lastModified();
        long currentTime = Instant.now(Clock.systemUTC()).toEpochMilli();
        return (currentTime - modificationTime) < threshold;
    }
}
