package com.enjin.officialplugin.threaded;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;

import org.bukkit.Bukkit;

import com.enjin.officialplugin.EnjinMinecraftPlugin;

public class DownloadPluginThread implements Runnable {
	
	String downloadlocation = "";
	File destination;
	EnjinMinecraftPlugin plugin;
	String versionnumber;
	
	public DownloadPluginThread(String downloadlocation, String versionnumber, File destination, EnjinMinecraftPlugin plugin) {
		this.downloadlocation = downloadlocation;
		this.versionnumber = versionnumber;
		this.destination = destination;
		this.plugin = plugin;
	}

	@Override
	public void run() {
		File tempfile = new File(downloadlocation + File.separator + "EnjinMinecraftPlugin.jar.part");
		try {
			URL website;
			if(EnjinMinecraftPlugin.bukkitversion) {
				String[] versionsplit = versionnumber.split(",");
				versionnumber = versionsplit[0];
				website = new URL(EnjinMinecraftPlugin.bukkitupdatejar + versionsplit[1]);
			}else {
				plugin.debug("Connecting to url " + EnjinMinecraftPlugin.updatejar + versionnumber + "/EnjinMinecraftPlugin.jar");
				website = new URL(EnjinMinecraftPlugin.updatejar + versionnumber + "/EnjinMinecraftPlugin.jar");
			}
		    ReadableByteChannel rbc = Channels.newChannel(website.openStream());
		    FileOutputStream fos = new FileOutputStream(tempfile);
		    fos.getChannel().transferFrom(rbc, 0, 1 << 24);
		    fos.close();
		    if(destination.delete() && tempfile.renameTo(destination)) {
		    	plugin.hasupdate = true;
		    	plugin.newversion = versionnumber;
		    	Bukkit.getLogger().warning("[Enjin Minecraft Plugin] Enjin Minecraft plugin was updated to version " + versionnumber + ". Please restart your server.");
		    	return;
		    }else {
		    	plugin.updatefailed = true;
		    	Bukkit.getLogger().warning("[Enjin Minecraft Plugin] Unable to update to new version. Please update manually!");
		    }
		} catch (IOException e) {
			e.printStackTrace();
		}
	    plugin.hasupdate = false;
	}

}
