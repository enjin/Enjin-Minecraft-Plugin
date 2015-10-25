package com.enjin.bukkit.threaded;

import java.util.concurrent.ConcurrentLinkedQueue;

import org.bukkit.Bukkit;
import org.bukkit.event.Event;

import com.enjin.bukkit.EnjinMinecraftPlugin;

public class AsyncToSyncEventThrower implements Runnable {

    ConcurrentLinkedQueue<Event> event = new ConcurrentLinkedQueue<Event>();
    boolean hasrun = false;
    EnjinMinecraftPlugin plugin;

    public AsyncToSyncEventThrower(EnjinMinecraftPlugin plugin) {
        this.plugin = plugin;
    }

    public AsyncToSyncEventThrower(Event event) {
        this.event.add(event);
    }

    public void addEvent(Event event) {
        this.event.add(event);
    }

    @Override
    public void run() {
        long starttime = System.currentTimeMillis();
        while (!event.isEmpty()) {
            Event ev = event.poll();
            if (ev != null) {
                Bukkit.getServer().getPluginManager().callEvent(ev);
            }
            long timeelapsed = System.currentTimeMillis() - starttime;
            //We only want it to take less than half a tick, otherwise,
            //let's schedule it to do the rest later so we don't lag
            //the server out.
            if (timeelapsed > 25) {
                plugin.getServer().getScheduler().scheduleSyncDelayedTask(plugin, plugin.eventthrower, 5);
                return;
            }
        }
    }

    boolean hasRun() {
        return event.isEmpty();
    }

}
