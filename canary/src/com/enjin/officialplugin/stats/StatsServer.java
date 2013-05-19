package com.enjin.officialplugin.stats;

import java.util.Collection;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import net.canarymod.Canary;
import net.canarymod.api.world.World;

import com.enjin.officialplugin.EnjinMinecraftPlugin;
import com.enjin.proto.stats.EnjinStats;
import com.enjin.proto.stats.EnjinStats.Server.KickPlayer;

public class StatsServer {
	
	EnjinMinecraftPlugin plugin;
	
	long lastserverstarttime = System.currentTimeMillis();
	int totalkicks = 0;
	ConcurrentHashMap<String, Integer> playerkicks = new ConcurrentHashMap<String, Integer>();
	int creeperexplosions = 0;
	
	public StatsServer(EnjinMinecraftPlugin plugin) {
		this.plugin = plugin;
	}
	
	public StatsServer(EnjinMinecraftPlugin plugin, EnjinStats.Server serverstats) {
		this.plugin = plugin;
		totalkicks = serverstats.getTotalkicks();
		creeperexplosions = serverstats.getCreeperexplosions();
		List<KickPlayer> kicks = serverstats.getPlayerskickedList();
		for(KickPlayer kick : kicks) {
			playerkicks.put(kick.getName(), kick.getCount());
		}
	}
	
	public long getLastserverstarttime() {
		return lastserverstarttime;
	}


	public void setLastserverstarttime(long lastserverstarttime) {
		this.lastserverstarttime = lastserverstarttime;
	}


	public int getTotalkicks() {
		return totalkicks;
	}
	
	public void addKick(String playername) {
		totalkicks++;
		int playerkick = 0;
		if(playerkicks.containsKey(playername)) {
			playerkick = playerkicks.get(playername);
		}
		playerkick++;
		playerkicks.put(playername, new Integer(playerkick));
	}


	public void setTotalkicks(int totalkicks) {
		this.totalkicks = totalkicks;
	}


	public int getCreeperexplosions() {
		return creeperexplosions;
	}
	
	public void addCreeperExplosion() {
		creeperexplosions++;
	}

	public void setCreeperexplosions(int creeperexplosions) {
		this.creeperexplosions = creeperexplosions;
	}
	
	public EnjinStats.Server.Builder getSerialized() {
		EnjinStats.Server.Builder serverbuilder = EnjinStats.Server.newBuilder();
		serverbuilder.setCreeperexplosions(creeperexplosions);
		int totalentities = 0;
		Collection<World> worlds = Canary.getServer().getWorldManager().getAllWorlds();
		for(World world : worlds) {
			totalentities += world.getEntityLivingList().size();
		}
		serverbuilder.setEntities(totalentities);
		Runtime runtime = Runtime.getRuntime();
		long memused = runtime.totalMemory()/(1024*1024);
		long maxmemory = runtime.maxMemory()/(1024*1024);
		serverbuilder.setMemoryused((int) memused);
		EnjinStats.Server.ServerInformation serverinfo = EnjinStats.Server.ServerInformation.newBuilder().
				setMaxmemory((int) maxmemory).setJavaversion(System.getProperty("java.version") + " " + System.getProperty("java.vendor"))
				.setOperatingsystem(System.getProperty("os.name") + " " + System.getProperty("os.version") + " " + System.getProperty("os.arch"))
				.setCorecount(runtime.availableProcessors())
				.setServerversion(/*Canary.getServer().getVersion()*/"1.5.2")//TODO: add in correct versions!
				.setLaststarttime((int)(lastserverstarttime/1000)).build();
		serverbuilder.setServerinfo(serverinfo);
		Set<Entry<String, Integer>> kicks = playerkicks.entrySet();
		for(Entry<String, Integer> kick : kicks) {
			serverbuilder.addPlayerskicked(KickPlayer.newBuilder().setName(kick.getKey()).setCount(kick.getValue().intValue()));
		}
		serverbuilder.setTotalkicks(totalkicks);
		return serverbuilder;
	}

	public ConcurrentHashMap<String, Integer> getPlayerkicks() {
		return playerkicks;
	}

	public void reset() {
		totalkicks = 0;
		playerkicks.clear();
		creeperexplosions = 0;
	}
}