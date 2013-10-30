package com.enjin.officialplugin.threaded;

import java.util.concurrent.ConcurrentLinkedQueue;

import org.bukkit.Bukkit;
import org.bukkit.event.Event;

import com.enjin.officialplugin.EnjinMinecraftPlugin;

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
		while(!event.isEmpty()) {
			Event ev = event.poll();
			Bukkit.getServer().getPluginManager().callEvent(ev);
		}
	}
	
	boolean hasRun() {
		return event.isEmpty();
	}

}
