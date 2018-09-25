/**
 *
 */
package com.enjin.sponge.tasks;

import lombok.Getter;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.LinkedList;

public class TPSMonitor implements Runnable {
    @Getter
    private static final DecimalFormat decimalFormat = new DecimalFormat("#.####");

    static {
        decimalFormat.setRoundingMode(RoundingMode.HALF_UP);
    }

    @Getter
    private static TPSMonitor         instance;
    private        LinkedList<Double> list     = new LinkedList<>();
    private        long               last     = System.currentTimeMillis();
    private        int                interval = 40;
    private        int                max      = 25;

    public TPSMonitor() {
        TPSMonitor.instance = this;
    }

    @Override
    public synchronized void run() {
        long   current  = System.currentTimeMillis();
        double duration = ((double) current - (double) last) / 1000f;
        double tps      = (double) interval / duration;

        if (list.size() >= max) {
            list.pop();
        }

        list.add(tps);
        last = current;
    }

    public synchronized double getTPSAverage() {
        if (list.size() > 0) {
            double averageTps = 0;

            for (Double tps : list) {
                averageTps += tps.floatValue();
            }

            double average = averageTps / (double) list.size();
            return average > 20.0 ? 20.0 : average;
        }

        return -1;
    }

    public synchronized double getLastTPSMeasurement() {
        if (list.size() > 0) {
            double latest = list.getLast();
            return latest > 20.0 ? 20.0 : latest;
        }

        return -1;
    }

    public synchronized void clearTPS() {
        double last = getLastTPSMeasurement();
        list.clear();
        list.add(last);
    }
}
