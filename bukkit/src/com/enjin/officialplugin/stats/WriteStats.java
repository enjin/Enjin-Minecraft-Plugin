package com.enjin.officialplugin.stats;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Map.Entry;

import com.enjin.officialplugin.EnjinMinecraftPlugin;
import com.enjin.proto.stats.EnjinStats;
import com.sun.xml.internal.messaging.saaj.util.ByteOutputStream;

public class WriteStats {
	
	EnjinMinecraftPlugin plugin;
	
	public WriteStats(EnjinMinecraftPlugin plugin) {
		this.plugin = plugin;
	}
	
	public boolean write(String file) {
		EnjinStats.Server.Builder stats = plugin.serverstats.getSerialized();
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
	
	public byte[] write() {
		EnjinStats.Server.Builder stats = plugin.serverstats.getSerialized();
		for(Entry<String, StatsPlayer> eplayer : plugin.playerstats.entrySet()) {
			stats.addPlayers(eplayer.getValue().getSerialized());
		}
		ByteOutputStream output = new ByteOutputStream();
		try {
			stats.build().writeTo(output);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return output.getBytes();
	}

}
