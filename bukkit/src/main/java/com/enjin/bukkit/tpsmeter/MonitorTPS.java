/**
 *
 */
package com.enjin.bukkit.tpsmeter;

import java.util.LinkedList;

import com.enjin.bukkit.EnjinMinecraftPlugin;

public class MonitorTPS implements Runnable {
    private LinkedList<Double> list = new LinkedList<Double>();
    private long last = System.currentTimeMillis();
    private int interval = 40;
    private int max = 25;

    @Override
    public synchronized void run() {
        long current = System.currentTimeMillis();
        double duration = ((double) current - (double) last) / 1000f;
        double tps = (double) interval / duration;

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

            return averageTps / (double) list.size();
        }

        return -1;
    }

    public synchronized double getLastTPSMeasurement() {
        if (list.size() > 0) {
            return list.getLast();
        }

        return -1;
    }

    public synchronized void clearTPS() {
        double last = getLastTPSMeasurement();
        list.clear();
        list.add(last);
    }
}
