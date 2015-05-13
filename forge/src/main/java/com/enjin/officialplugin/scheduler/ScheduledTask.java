package com.enjin.officialplugin.scheduler;

public class ScheduledTask {

    Runnable task;
    boolean repeatTask = false;
    boolean async = false;
    int ticksToExecution = 0;
    int ticksBetweenExecution = 0;
    TaskWrapper wrapper;
    Thread thread;

    public ScheduledTask(Runnable task, int waitTime) {
        this.task = task;
        ticksToExecution = waitTime;
    }

    public ScheduledTask(Runnable task, int initialWaitTime, int waitTime) {
        this.task = task;
        ticksToExecution = initialWaitTime;
        ticksBetweenExecution = waitTime;
        repeatTask = true;
    }

    public void run() {
        if (repeatTask) {
            if (async) {
                if (wrapper == null) {
                    wrapper = new TaskWrapper(task);
                    thread = new Thread(wrapper);
                    thread.start();
                } else {
                    thread.interrupt();
                }
            } else {
                task.run();
            }
        } else {
            if (async) {
                Thread t = new Thread(task);
                t.start();
            } else {
                task.run();
            }
        }
    }

    public boolean repeatTask() {
        return repeatTask;
    }

    public void stop() {
        if (repeatTask) {
            if (wrapper != null) {
                wrapper.stopIt();
                thread.interrupt();
            }
        }
        wrapper = null;
        thread = null;
        task = null;
    }

    public int getTicksToExecution() {
        return ticksToExecution;
    }

    public void setTicksToExecution(int ticksToExecution) {
        this.ticksToExecution = ticksToExecution;
    }

    public int getTicksBetweenExecution() {
        return ticksBetweenExecution;
    }

    public void removeTickToExecution() {
        ticksToExecution--;
    }

    public void setAsync(boolean value) {
        async = value;
    }

    public boolean isAsync() {
        return async;
    }
}
