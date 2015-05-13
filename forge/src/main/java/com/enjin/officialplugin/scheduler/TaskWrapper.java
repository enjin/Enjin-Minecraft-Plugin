package com.enjin.officialplugin.scheduler;

public class TaskWrapper implements Runnable {

    Runnable task;
    boolean running = true;

    public TaskWrapper(Runnable task) {
        this.task = task;
    }

    public synchronized void stopIt() {
        running = false;
    }

    @Override
    public synchronized void run() {
        while (running) {
            task.run();
            try {
                wait();
            } catch (InterruptedException e) {
            }
        }
    }

}
