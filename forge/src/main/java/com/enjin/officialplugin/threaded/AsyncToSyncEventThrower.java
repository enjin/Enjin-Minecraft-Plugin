package com.enjin.officialplugin.threaded;

import java.util.concurrent.ConcurrentLinkedQueue;

import com.enjin.officialplugin.EnjinMinecraftPlugin;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.Event;

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
        while (!event.isEmpty()) {
            Event ev = event.poll();
            MinecraftForge.EVENT_BUS.post(ev);
        }
    }

    boolean hasRun() {
        return event.isEmpty();
    }

}
