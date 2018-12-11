package com.enjin.core.util;

public class Timer {

    private long start = 0;
    private long end = 0;

    public void start() {
        start = System.currentTimeMillis();
    }

    public void stop() {
        end = System.currentTimeMillis();
    }

    public long getDifference() {
        return end - start;
    }
}
