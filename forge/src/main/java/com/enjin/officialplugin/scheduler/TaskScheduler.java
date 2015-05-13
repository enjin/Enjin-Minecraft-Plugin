package com.enjin.officialplugin.scheduler;

import java.util.Collection;
import java.util.EnumSet;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.enjin.officialplugin.EnjinMinecraftPlugin;
import com.enjin.officialplugin.threaded.CommandExecuter;
import com.enjin.officialplugin.threaded.PeriodicEnjinTask;

import cpw.mods.fml.common.ITickHandler;
import cpw.mods.fml.common.TickType;

public class TaskScheduler implements ITickHandler {

    int nexttaskid = 1;
    ConcurrentHashMap<Integer, ScheduledTask> tasks = new ConcurrentHashMap<Integer, ScheduledTask>();

    @Override
    public void tickStart(EnumSet<TickType> type, Object... tickData) {
        Set<Entry<Integer, ScheduledTask>> thetasks = tasks.entrySet();
        for (Entry<Integer, ScheduledTask> taskentry : thetasks) {
            ScheduledTask task = taskentry.getValue();
            if (task.getTicksToExecution() <= 0) {
                task.run();
                if (task.repeatTask()) {
                    task.setTicksToExecution(task.getTicksBetweenExecution());
                } else {
                    tasks.remove(taskentry.getKey());
                }
            } else {
                task.removeTickToExecution();
            }
        }
    }

    @Override
    public void tickEnd(EnumSet<TickType> type, Object... tickData) {
        // TODO Auto-generated method stub

    }

    @Override
    public EnumSet<TickType> ticks() {
        EnumSet<TickType> t = (EnumSet.allOf(TickType.class));
        t.remove(TickType.SERVER);
        return EnumSet.complementOf(t);
    }

    @Override
    public String getLabel() {
        return "EnjinTaskScheduler";
    }

    int nextTaskID() {
        return ++nexttaskid;
    }

    public int runTaskTimerAsynchronously(Runnable task, int tickstostart, int ticksToWait) {
        ScheduledTask st = new ScheduledTask(task, tickstostart, ticksToWait);

        int taskID = nextTaskID();
        tasks.put(taskID, st);
        return taskID;
    }

    public boolean cancelTask(int synctaskid) {
        Integer taskid = new Integer(synctaskid);
        ScheduledTask st = tasks.remove(taskid);
        if (st != null) {
            st.stop();
            return true;
        }
        return false;
    }

    public void cancelAllTasks() {
        Set<Entry<Integer, ScheduledTask>> thetasks = tasks.entrySet();
        for (Entry<Integer, ScheduledTask> taskentry : thetasks) {
            ScheduledTask task = taskentry.getValue();
            task.stop();
            tasks.remove(taskentry.getKey());
        }
        thetasks.clear();
    }

    public int scheduleSyncDelayedTask(Runnable task) {
        ScheduledTask st = new ScheduledTask(task, 0);
        st.setAsync(false);

        int taskID = nextTaskID();
        tasks.put(taskID, st);
        return taskID;
    }

    public int scheduleSyncDelayedTask(Runnable task, int delay) {
        ScheduledTask st = new ScheduledTask(task, delay);
        st.setAsync(false);

        int taskID = nextTaskID();
        tasks.put(taskID, st);
        return taskID;
    }
}
