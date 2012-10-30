package com.enjin.officialplugin.stats;

import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.entity.EntityType;

public class StatsServer {
	
	ConcurrentHashMap<EntityType, Integer> creaturekills = new ConcurrentHashMap<EntityType, Integer>();

}
