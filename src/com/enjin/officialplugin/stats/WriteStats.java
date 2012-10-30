package com.enjin.officialplugin.stats;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Map.Entry;

import com.enjin.officialplugin.EnjinMinecraftPlugin;
import com.enjin.proto.stats.EnjinStats;

public class WriteStats {
	
	EnjinMinecraftPlugin plugin;
	
	public WriteStats(EnjinMinecraftPlugin plugin) {
		this.plugin = plugin;
	}
	
	public boolean write(String file) {
		EnjinStats.Server.Builder stats = EnjinStats.Server.newBuilder();
		for(Entry<String, StatsPlayer> eplayer : plugin.playerstats.entrySet()) {
			stats.addPlayers(eplayer.getValue().getSerialized());
		}
		try {
			FileOutputStream output = new FileOutputStream(file);
			stats.build().writeTo(output);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return true;
	}

}
